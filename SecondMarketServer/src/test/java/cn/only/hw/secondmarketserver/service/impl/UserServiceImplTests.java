package cn.only.hw.secondmarketserver.service.impl;

import cn.only.hw.secondmarketserver.dao.UserDao;
import cn.only.hw.secondmarketserver.entity.User;
import cn.only.hw.secondmarketserver.util.Result;
import cn.only.hw.secondmarketserver.util.UserSecurityService;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTests {

    @Mock
    private UserDao userDao;

    @Mock
    private UserSecurityService userSecurityService;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        initializeTableInfo(User.class);
        userService = new UserServiceImpl();
        ReflectionTestUtils.setField(userService, "baseMapper", userDao);
        ReflectionTestUtils.setField(userService, "userDao", userDao);
        ReflectionTestUtils.setField(userService, "userSecurityService", userSecurityService);
    }

    @Test
    void shouldRejectInvalidPhoneDuringRegister() {
        User user = buildRegisterUser();
        user.setTel("123456");

        Result<String> result = userService.register(user);

        assertEquals(Integer.valueOf(0), result.getCode());
        assertEquals("请输入正确的手机号", result.getMsg());
        verify(userDao, never()).selectCount(any());
        verify(userDao, never()).insert(any(User.class));
    }

    @Test
    void shouldRejectInvalidIdcardDuringRegister() {
        User user = buildRegisterUser();
        user.setTel("13800138000");
        user.setIdcard("110101199913011234");

        Result<String> result = userService.register(user);

        assertEquals(Integer.valueOf(0), result.getCode());
        assertEquals("请输入正确的身份证号", result.getMsg());
        verify(userDao, never()).selectCount(any());
        verify(userDao, never()).insert(any(User.class));
    }

    @Test
    void shouldNormalizeLowercaseIdcardBeforeSave() {
        User latestUser = new User();
        latestUser.setId(9);

        when(userSecurityService.prepareUserForWrite(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(userDao.selectCount(any())).thenReturn(0);
        when(userDao.selectOne(any())).thenReturn(latestUser);
        when(userDao.insert(any(User.class))).thenReturn(1);

        User user = buildRegisterUser();
        user.setTel("13800138000");
        user.setIdcard("11010519491231002x");

        Result<String> result = userService.register(user);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userDao).insert(captor.capture());

        assertEquals(Integer.valueOf(1), result.getCode());
        assertEquals("11010519491231002X", captor.getValue().getIdcard());
        assertEquals(Integer.valueOf(10), captor.getValue().getId());
    }

    private User buildRegisterUser() {
        User user = new User();
        user.setAccount("student001");
        user.setPassword("password123");
        return user;
    }

    private void initializeTableInfo(Class<?> entityClass) {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), ""),
                entityClass
        );
    }
}
