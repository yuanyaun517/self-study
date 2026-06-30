package cn.only.hw.secondmarketserver.service.impl;

import cn.only.hw.secondmarketserver.dao.UserDao;
import cn.only.hw.secondmarketserver.entity.User;
import cn.only.hw.secondmarketserver.service.UserService;
import cn.only.hw.secondmarketserver.util.Result;
import cn.only.hw.secondmarketserver.util.UserSecurityService;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 用户服务实现类
 * 提供用户相关的业务逻辑处理，包括：
 * - 用户注册、登录
 * - 用户信息的增删改查
 * - 用户余额管理（充值、提现）
 * - 敏感信息加密处理
 *
 * @author 李淑娟
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserDao, User> implements UserService {

    /** 中国大陆手机号正则表达式：1开头，第二位3-9，后面9位数字 */
    private static final Pattern MAINLAND_PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    
    /** 中国大陆身份证号正则表达式：17位数字 + 1位数字或X */
    private static final Pattern MAINLAND_IDCARD_PATTERN = Pattern.compile("^\\d{17}[\\dX]$");
    
    /** 身份证校验码计算权重系数 */
    private static final int[] ID_CARD_WEIGHTS = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    
    /** 身份证校验码对照表 */
    private static final char[] ID_CARD_CHECK_CODES = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
    
    /** 有效的省份代码集合（包含港澳台） */
    private static final Set<String> VALID_PROVINCE_CODES = new HashSet<>(Arrays.asList(
            "11", "12", "13", "14", "15",
            "21", "22", "23",
            "31", "32", "33", "34", "35", "36", "37",
            "41", "42", "43", "44", "45", "46",
            "50", "51", "52", "53", "54",
            "61", "62", "63", "64", "65",
            "71", "81", "82", "91"
    ));

    /** 用户数据访问对象 */
    @Autowired
    private UserDao userDao;

    /** 用户安全服务，用于敏感信息加密和解密 */
    @Autowired
    private UserSecurityService userSecurityService;

    /**
     * 保存用户信息
     * 在保存前会对敏感信息进行加密处理
     *
     * @param entity 用户实体对象
     * @return 是否保存成功
     */
    @Override
    public boolean save(User entity) {
        return super.save(userSecurityService.prepareUserForWrite(entity));
    }

    /**
     * 根据ID更新用户信息
     * 在更新前会对敏感信息进行加密处理
     *
     * @param entity 用户实体对象
     * @return 是否更新成功
     */
    @Override
    public boolean updateById(User entity) {
        return super.updateById(userSecurityService.prepareUserForWrite(entity));
    }

    /**
     * 根据ID查询用户信息
     * 查询结果会对敏感信息进行脱敏处理
     *
     * @param id 用户ID
     * @return 用户实体对象
     */
    @Override
    public User getById(Serializable id) {
        return normalizeUserForRead(super.getById(id));
    }

    /**
     * 查询所有用户列表
     * 查询结果会对敏感信息进行脱敏处理
     *
     * @return 用户列表
     */
    @Override
    public List<User> list() {
        return normalizeUserListForRead(super.list());
    }

    /**
     * 根据条件查询用户列表
     * 查询结果会对敏感信息进行脱敏处理
     *
     * @param queryWrapper 查询条件包装器
     * @return 用户列表
     */
    @Override
    public List<User> list(Wrapper<User> queryWrapper) {
        return normalizeUserListForRead(super.list(queryWrapper));
    }

    /**
     * 用户登录验证
     * 验证账号和密码是否正确，并升级旧版本的加密字段
     *
     * @param user 包含账号和密码的用户对象
     * @return 登录成功的用户对象，失败返回null
     */
    @Override
    public User login(User user) {
        // 获取并清理账号和密码
        String account = trimToNull(user == null ? null : user.getAccount());
        String password = trimToNull(user == null ? null : user.getPassword());
        if (account == null || password == null) {
            return null;
        }

        // 构建查询条件：按账号查询，按ID降序，取最新的一条记录
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getAccount, account);
        queryWrapper.orderByDesc(User::getId);
        queryWrapper.last("limit 1");
        User loginUser = userDao.selectOne(queryWrapper);
        
        // 验证用户是否存在且密码匹配
        if (loginUser == null || !userSecurityService.matchesPassword(password, loginUser.getPassword())) {
            return null;
        }

        // 升级旧版本的敏感字段加密
        upgradeLegacySensitiveFields(loginUser, password);
        // 返回脱敏后的用户信息
        return normalizeUserForRead(loginUser);
    }

    /**
     * 用户注册
     * 验证用户信息合法性，检查账号唯一性，保存新用户
     *
     * @param user 待注册的用户对象
     * @return 注册结果，包含成功/失败信息和消息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<String> register(User user) {
        // 验证用户对象不为空
        if (user == null) {
            return Result.error("user payload is empty");
        }

        // 获取并验证账号和密码
        String account = trimToNull(user.getAccount());
        String password = trimToNull(user.getPassword());
        if (account == null || password == null) {
            return Result.error("account or password is empty");
        }

        // 设置用户基本信息
        user.setAccount(account);
        user.setPassword(password);
        user.setNickname(defaultIfBlank(user.getNickname(), account));
        user.setSex(trimToNull(user.getSex()));
        user.setTel(trimToNull(user.getTel()));
        user.setIdcard(normalizeIdcard(user.getIdcard()));
        user.setCollege(trimToNull(user.getCollege()));
        user.setGrade(trimToNull(user.getGrade()));
        user.setRoomnumb(trimToNull(user.getRoomnumb()));
        user.setIcon(trimToNull(user.getIcon()));
        user.setBalance(normalizeBalance(user.getBalance()));

        // 验证联系方式（手机号和身份证）
        String contactValidationMessage = validateRegisterContact(user.getTel(), user.getIdcard());
        if (contactValidationMessage != null) {
            return Result.error(contactValidationMessage);
        }

        // 检查账号是否已存在
        if (existsByAccount(account)) {
            return Result.error("账号已存在");
        }

        // 生成新的用户ID
        if (user.getId() == null) {
            user.setId(resolveNextUserId());
        }

        // 保存用户并返回结果
        if (save(user)) {
            return Result.success("注册成功");
        }
        return Result.error("注册失败");
    }

    /**
     * 用户余额充值
     *
     * @param userId 用户ID
     * @param amount 充值金额
     * @return 充值后的余额
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Double rechargeBalance(Integer userId, Double amount) {
        return changeBalance(userId, amount, true);
    }

    /**
     * 用户余额提现
     *
     * @param userId 用户ID
     * @param amount 提现金额
     * @return 提现后的余额
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Double withdrawBalance(Integer userId, Double amount) {
        return changeBalance(userId, amount, false);
    }

    /**
     * 检查账号是否已存在
     *
     * @param account 账号
     * @return 是否存在
     */
    private boolean existsByAccount(String account) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getAccount, account);
        queryWrapper.last("limit 1");
        return userDao.selectCount(queryWrapper) > 0;
    }

    /**
     * 获取下一个用户ID（当前最大ID + 1）
     *
     * @return 下一个用户ID
     */
    private Integer resolveNextUserId() {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(User::getId);
        queryWrapper.orderByDesc(User::getId);
        queryWrapper.last("limit 1");
        User latestUser = userDao.selectOne(queryWrapper);
        if (latestUser == null || latestUser.getId() == null) {
            return 1;
        }
        return latestUser.getId() + 1;
    }

    /**
     * 升级旧版本的敏感字段加密
     * 如果用户使用旧的加密方式，则重新加密并更新数据库
     *
     * @param storedUser 数据库中存储的用户对象
     * @param rawPassword 原始密码
     */
    private void upgradeLegacySensitiveFields(User storedUser, String rawPassword) {
        User upgradeUser = userSecurityService.buildUpgradePayload(storedUser, rawPassword);
        if (upgradeUser != null) {
            super.updateById(upgradeUser);
        }
    }

    /**
     * 对用户信息进行脱敏处理，准备返回给前端
     *
     * @param user 用户对象
     * @return 脱敏后的用户对象
     */
    private User normalizeUserForRead(User user) {
        return userSecurityService.prepareUserForRead(user);
    }

    /**
     * 对用户列表进行脱敏处理
     *
     * @param userList 用户列表
     * @return 脱敏后的用户列表
     */
    private List<User> normalizeUserListForRead(List<User> userList) {
        if (userList != null) {
            userList.forEach(this::normalizeUserForRead);
        }
        return userList;
    }

    /**
     * 如果值为空则返回默认值
     *
     * @param value 原始值
     * @param defaultValue 默认值
     * @return 非空值或默认值
     */
    private String defaultIfBlank(String value, String defaultValue) {
        String trimmedValue = trimToNull(value);
        return trimmedValue == null ? defaultValue : trimmedValue;
    }

    /**
     * 验证注册时的联系方式（手机号和身份证）
     * 验证规则：
     * 1. 手机号必须符合中国大陆手机号格式
     * 2. 身份证号必须符合格式规范（18位、省份代码、出生日期、校验码）
     *
     * @param tel 手机号
     * @param idcard 身份证号
     * @return 验证失败返回错误信息，成功返回null
     */
    private String validateRegisterContact(String tel, String idcard) {
        // 验证手机号格式
        if (tel != null && !MAINLAND_PHONE_PATTERN.matcher(tel).matches()) {
            return "请输入正确的手机号";
        }

        // 身份证号为空则跳过验证
        if (idcard == null) {
            return null;
        }

        // 验证身份证号格式（17位数字+1位数字或X）
        if (!MAINLAND_IDCARD_PATTERN.matcher(idcard).matches()) {
            return "请输入正确的身份证号";
        }

        // 验证省份代码
        if (!VALID_PROVINCE_CODES.contains(idcard.substring(0, 2))) {
            return "请输入正确的身份证号";
        }

        // 验证出生日期合法性
        if (!isValidIdcardBirthDate(idcard.substring(6, 14))) {
            return "请输入正确的身份证号";
        }

        // 验证校验码
        if (!isValidIdcardChecksum(idcard)) {
            return "请输入正确的身份证号";
        }

        return null;
    }

    /**
     * 修改用户余额（充值或提现）
     *
     * @param userId 用户ID
     * @param amount 金额
     * @param increase true表示充值，false表示提现
     * @return 修改后的余额
     * @throws IllegalArgumentException 参数非法时抛出
     * @throws IllegalStateException 更新失败时抛出
     */
    private Double changeBalance(Integer userId, Double amount, boolean increase) {
        // 验证用户ID
        if (userId == null) {
            throw new IllegalArgumentException("用户编号不能为空");
        }

        // 规范化交易金额
        BigDecimal changeAmount = normalizeTradeAmount(amount);
        
        // 查询当前用户
        User currentUser = super.getById(userId);
        if (currentUser == null) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 计算新余额
        BigDecimal currentBalance = toBalanceAmount(currentUser.getBalance());
        BigDecimal nextBalance = increase
                ? currentBalance.add(changeAmount)  // 充值
                : currentBalance.subtract(changeAmount);  // 提现

        // 验证余额是否充足（提现时）
        if (nextBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("当前余额不足，无法提现");
        }

        // 更新用户余额
        User updateUser = new User();
        updateUser.setId(userId);
        updateUser.setBalance(nextBalance.setScale(2, RoundingMode.HALF_UP).doubleValue());

        if (!super.updateById(updateUser)) {
            throw new IllegalStateException("update balance failed");
        }

        return updateUser.getBalance();
    }

    /**
     * 去除字符串首尾空格，如果为空则返回null
     *
     * @param value 原始字符串
     * @return 清理后的字符串，空字符串返回null
     */
    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    /**
     * 规范化身份证号：转为大写
     *
     * @param value 身份证号
     * @return 大写格式的身份证号，空值返回null
     */
    private String normalizeIdcard(String value) {
        String trimmedValue = trimToNull(value);
        return trimmedValue == null ? null : trimmedValue.toUpperCase();
    }

    /**
     * 验证身份证中的出生日期是否合法
     *
     * @param birthdayText 8位日期字符串（格式：yyyyMMdd）
     * @return 是否为合法日期且不晚于今天
     */
    private boolean isValidIdcardBirthDate(String birthdayText) {
        int year = Integer.parseInt(birthdayText.substring(0, 4));
        int month = Integer.parseInt(birthdayText.substring(4, 6));
        int day = Integer.parseInt(birthdayText.substring(6, 8));
        
        // 年份不能早于1900年
        if (year < 1900) {
            return false;
        }

        try {
            LocalDate birthday = LocalDate.of(year, month, day);
            return !birthday.isAfter(LocalDate.now());
        } catch (DateTimeException ex) {
            return false;
        }
    }

    /**
     * 验证身份证号校验码是否正确
     * 算法：前17位分别乘以权重系数，求和后取模11，对应校验码表
     *
     * @param idcard 18位身份证号
     * @return 校验码是否正确
     */
    private boolean isValidIdcardChecksum(String idcard) {
        int sum = 0;
        for (int index = 0; index < ID_CARD_WEIGHTS.length; index++) {
            sum += Character.getNumericValue(idcard.charAt(index)) * ID_CARD_WEIGHTS[index];
        }
        return ID_CARD_CHECK_CODES[sum % 11] == idcard.charAt(17);
    }

    /**
     * 规范化交易金额
     * 验证金额必须为正数，保留两位小数
     *
     * @param amount 原始金额
     * @return 规范化后的金额
     * @throws IllegalArgumentException 金额不合法时抛出
     */
    private BigDecimal normalizeTradeAmount(Double amount) {
        if (amount == null || !Double.isFinite(amount) || amount <= 0) {
            throw new IllegalArgumentException("金额必须大于 0");
        }
        BigDecimal normalizedAmount = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
        if (normalizedAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("金额必须大于 0");
        }
        return normalizedAmount;
    }

    /**
     * 将余额转换为BigDecimal格式
     *
     * @param balance 余额
     * @return BigDecimal格式的余额
     */
    private BigDecimal toBalanceAmount(Double balance) {
        return BigDecimal.valueOf(normalizeBalance(balance)).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 规范化余额值
     * 空值或负数返回0，保留两位小数
     *
     * @param balance 原始余额
     * @return 规范化后的余额
     */
    private Double normalizeBalance(Double balance) {
        if (balance == null) {
            return 0D;
        }
        if (!Double.isFinite(balance) || balance < 0) {
            return 0D;
        }
        return BigDecimal.valueOf(balance)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
