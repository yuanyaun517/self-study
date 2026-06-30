package cn.only.hw.secondmarketserver.util;

import cn.only.hw.secondmarketserver.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

/**
 * 用户信息安全处理服务
 * 负责密码哈希、敏感字段（手机号、身份证）的加密和解密
 */
@Component
public class UserSecurityService {

    // 加密数据前缀标识
    private static final String ENCRYPTED_PREFIX = "ENC$";
    // 密码哈希前缀标识
    private static final String PASSWORD_PREFIX = "PBKDF2$";
    // 密码哈希算法
    private static final String PASSWORD_ALGORITHM = "PBKDF2WithHmacSHA1";
    // 密码哈希迭代次数
    private static final int PASSWORD_ITERATIONS = 65536;
    // 密码密钥长度（位）
    private static final int PASSWORD_KEY_LENGTH = 256;
    // AES密钥长度（字节）
    private static final int AES_KEY_LENGTH = 16;
    // 初始化向量长度（字节）
    private static final int IV_LENGTH = 16;
    // 盐值长度（字节）
    private static final int SALT_LENGTH = 16;

    // 安全随机数生成器
    private final SecureRandom secureRandom = new SecureRandom();
    // AES密钥规格
    private final SecretKeySpec aesKeySpec;

    /**
     * 构造函数，从配置中读取加密密钥并生成AES密钥
     * @param cryptoSecret 加密密钥字符串，默认值为 school-market-user-secret
     */
    public UserSecurityService(
            @Value("${security.crypto.secret:school-market-user-secret}") String cryptoSecret) {
        this.aesKeySpec = new SecretKeySpec(buildAesKey(cryptoSecret), "AES");
    }

    /**
     * 准备用户数据进行写入（保存或更新）
     * 对密码进行哈希处理，对手机号和身份证进行加密
     * @param user 用户对象
     * @return 处理后的用户对象
     */
    public User prepareUserForWrite(User user) {
        if (user == null) {
            return null;
        }
        // 密码哈希处理
        user.setPassword(hashPlainPassword(normalizeIncomingSensitiveField(user.getPassword())));
        // 手机号加密
        user.setTel(encryptPlainField(normalizeIncomingSensitiveField(user.getTel())));
        // 身份证号加密
        user.setIdcard(encryptPlainField(normalizeIncomingSensitiveField(user.getIdcard())));
        return user;
    }

    /**
     * 准备用户数据进行读取
     * 解密手机号和身份证，清空密码字段
     * @param user 用户对象
     * @return 处理后的用户对象
     */
    public User prepareUserForRead(User user) {
        if (user == null) {
            return null;
        }
        // 解密手机号
        user.setTel(decryptFieldIfNeeded(user.getTel()));
        // 解密身份证号
        user.setIdcard(decryptFieldIfNeeded(user.getIdcard()));
        // 清除密码字段，不返回给前端
        user.setPassword(null);
        return user;
    }

    /**
     * 验证密码是否匹配
     * @param rawPassword 原始密码
     * @param storedPassword 存储的密码哈希值
     * @return 是否匹配
     */
    public boolean matchesPassword(String rawPassword, String storedPassword) {
        if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(storedPassword)) {
            return false;
        }

        String normalizedPassword = normalizeIncomingSensitiveField(rawPassword);
        if (!StringUtils.hasText(normalizedPassword)) {
            return false;
        }
        normalizedPassword = normalizedPassword.trim();
        if (!isHashedPassword(storedPassword)) {
            return storedPassword.equals(normalizedPassword);
        }

        String[] parts = storedPassword.split("\\$");
        if (parts.length != 4) {
            return false;
        }

        try {
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = Base64.getDecoder().decode(parts[2]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[3]);
            byte[] actualHash = derivePasswordHash(normalizedPassword, salt, iterations);
            return MessageDigest.isEqual(expectedHash, actualHash);
        } catch (IllegalArgumentException | GeneralSecurityException ex) {
            throw new IllegalStateException("密码错误", ex);
        }
    }

    /**
     * 检查密码是否需要升级（从明文升级到哈希）
     * @param storedPassword 存储的密码
     * @return 是否需要升级
     */
    public boolean needsPasswordUpgrade(String storedPassword) {
        return StringUtils.hasText(storedPassword) && !isHashedPassword(storedPassword);
    }

    /**
     * 检查字段是否需要加密
     * @param value 字段值
     * @return 是否需要加密
     */
    public boolean needsFieldEncryption(String value) {
        return StringUtils.hasText(value) && !isEncrypted(value);
    }

    /**
     * 构建升级payload，用于将旧数据升级到新的安全格式
     * @param storedUser 存储的用户对象
     * @param rawPassword 原始密码
     * @return 需要更新的用户对象，如果不需要升级则返回null
     */
    public User buildUpgradePayload(User storedUser, String rawPassword) {
        if (storedUser == null || storedUser.getId() == null) {
            return null;
        }

        User upgradeUser = new User();
        upgradeUser.setId(storedUser.getId());

        boolean changed = false;
        if (needsPasswordUpgrade(storedUser.getPassword()) && StringUtils.hasText(rawPassword)) {
            upgradeUser.setPassword(hashPlainPassword(rawPassword.trim()));
            changed = true;
        }
        if (needsFieldEncryption(storedUser.getTel())) {
            upgradeUser.setTel(encryptPlainField(storedUser.getTel()));
            changed = true;
        }
        if (needsFieldEncryption(storedUser.getIdcard())) {
            upgradeUser.setIdcard(encryptPlainField(storedUser.getIdcard()));
            changed = true;
        }

        return changed ? upgradeUser : null;
    }

    /**
     * 对明文密码进行哈希处理
     * 使用PBKDF2算法加盐哈希
     * @param rawPassword 原始密码
     * @return 哈希后的密码字符串，格式：PBKDF2$迭代次数$盐值$哈希值
     */
    public String hashPlainPassword(String rawPassword) {
        if (rawPassword == null) {
            return null;
        }
        String normalizedPassword = normalizeIncomingSensitiveField(rawPassword);
        if (normalizedPassword == null || normalizedPassword.isEmpty()) {
            return normalizedPassword;
        }

        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);

        try {
            byte[] hash = derivePasswordHash(normalizedPassword, salt, PASSWORD_ITERATIONS);
            return PASSWORD_PREFIX
                    + PASSWORD_ITERATIONS
                    + "$"
                    + Base64.getEncoder().encodeToString(salt)
                    + "$"
                    + Base64.getEncoder().encodeToString(hash);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Failed to hash password", ex);
        }
    }

    /**
     * 对敏感字段进行加密（手机号、身份证等）
     * 使用AES/CBC/PKCS5Padding算法加密
     * @param value 待加密的明文值
     * @return 加密后的字符串，格式：ENC$Base64(IV+密文)
     */
    public String encryptPlainField(String value) {
        if (value == null) {
            return null;
        }
        String normalizedValue = normalizeIncomingSensitiveField(value);
        if (normalizedValue == null || normalizedValue.isEmpty()) {
            return normalizedValue;
        }

        byte[] iv = new byte[IV_LENGTH];
        secureRandom.nextBytes(iv);

        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, aesKeySpec, new IvParameterSpec(iv));
            byte[] encryptedValue = cipher.doFinal(normalizedValue.getBytes(StandardCharsets.UTF_8));

            byte[] encryptedPayload = new byte[iv.length + encryptedValue.length];
            System.arraycopy(iv, 0, encryptedPayload, 0, iv.length);
            System.arraycopy(encryptedValue, 0, encryptedPayload, iv.length, encryptedValue.length);
            return ENCRYPTED_PREFIX + Base64.getEncoder().encodeToString(encryptedPayload);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Failed to encrypt sensitive field", ex);
        }
    }

    /**
     * 如果字段已加密则进行解密，否则原样返回
     * @param value 待解密的字段值
     * @return 解密后的明文或原值
     */
    public String decryptFieldIfNeeded(String value) {
        if (!StringUtils.hasText(value) || !isEncrypted(value)) {
            return value;
        }

        try {
            byte[] encryptedPayload = Base64.getDecoder().decode(value.substring(ENCRYPTED_PREFIX.length()));
            byte[] iv = Arrays.copyOfRange(encryptedPayload, 0, IV_LENGTH);
            byte[] encryptedValue = Arrays.copyOfRange(encryptedPayload, IV_LENGTH, encryptedPayload.length);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, aesKeySpec, new IvParameterSpec(iv));
            byte[] decryptedValue = cipher.doFinal(encryptedValue);
            return new String(decryptedValue, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException | GeneralSecurityException ex) {
            throw new IllegalStateException("Failed to decrypt sensitive field", ex);
        }
    }

    /**
     * 规范化传入的敏感字段
     * 如果字段已加密则先解密，用于处理可能已加密的数据
     * @param value 字段值
     * @return 规范化后的值
     */
    public String normalizeIncomingSensitiveField(String value) {
        if (value == null) {
            return null;
        }
        if (value.isEmpty()) {
            return value;
        }
        return decryptFieldIfNeeded(value);
    }

    /**
     * 判断值是否已加密
     * @param value 待判断的值
     * @return 是否已加密
     */
    private boolean isEncrypted(String value) {
        return value != null && value.startsWith(ENCRYPTED_PREFIX);
    }

    /**
     * 判断密码是否已哈希
     * @param value 待判断的密码值
     * @return 是否已哈希
     */
    private boolean isHashedPassword(String value) {
        return value != null && value.startsWith(PASSWORD_PREFIX);
    }

    /**
     * 使用PBKDF2算法派生密码哈希值
     * @param rawPassword 原始密码
     * @param salt 盐值
     * @param iterations 迭代次数
     * @return 哈希后的字节数组
     */
    private byte[] derivePasswordHash(String rawPassword, byte[] salt, int iterations)
            throws GeneralSecurityException {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(PASSWORD_ALGORITHM);
        KeySpec spec = new PBEKeySpec(rawPassword.toCharArray(), salt, iterations, PASSWORD_KEY_LENGTH);
        return factory.generateSecret(spec).getEncoded();
    }

    /**
     * 根据密钥字符串构建AES密钥
     * 使用SHA-256对密钥字符串进行哈希，取前16字节作为AES密钥
     * @param secret 密钥字符串
     * @return AES密钥字节数组
     */
    private byte[] buildAesKey(String secret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedSecret = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
            return Arrays.copyOf(hashedSecret, AES_KEY_LENGTH);
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Failed to initialize AES key", ex);
        }
    }
}
