package cn.only.hw.secondmarketserver.util;

import cn.only.hw.secondmarketserver.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserSecurityServiceTests {

    private final UserSecurityService userSecurityService = new UserSecurityService("unit-test-secret");

    @Test
    void shouldHashPasswordAndVerify() {
        String hashedPassword = userSecurityService.hashPlainPassword("123456");

        assertNotEquals("123456", hashedPassword);
        assertTrue(userSecurityService.matchesPassword("123456", hashedPassword));
    }

    @Test
    void shouldEncryptAndDecryptSensitiveFields() {
        String encryptedTel = userSecurityService.encryptPlainField("13800138000");
        String encryptedIdcard = userSecurityService.encryptPlainField("110101199901011234");

        assertNotEquals("13800138000", encryptedTel);
        assertNotEquals("110101199901011234", encryptedIdcard);
        assertEquals("13800138000", userSecurityService.decryptFieldIfNeeded(encryptedTel));
        assertEquals("110101199901011234", userSecurityService.decryptFieldIfNeeded(encryptedIdcard));
    }

    @Test
    void shouldHandleEncryptedTransportFieldsWhenWritingUser() {
        User user = new User();
        user.setPassword(userSecurityService.encryptPlainField("123456"));
        user.setTel(userSecurityService.encryptPlainField("13800138000"));
        user.setIdcard(userSecurityService.encryptPlainField("110101199901011234"));

        User preparedUser = userSecurityService.prepareUserForWrite(user);

        assertTrue(userSecurityService.matchesPassword("123456", preparedUser.getPassword()));
        assertEquals("13800138000", userSecurityService.decryptFieldIfNeeded(preparedUser.getTel()));
        assertEquals("110101199901011234", userSecurityService.decryptFieldIfNeeded(preparedUser.getIdcard()));
    }

    @Test
    void shouldClearPasswordWhenReadingUser() {
        User user = new User();
        user.setPassword("123456");
        user.setTel(userSecurityService.encryptPlainField("13800138000"));
        user.setIdcard(userSecurityService.encryptPlainField("110101199901011234"));

        User normalizedUser = userSecurityService.prepareUserForRead(user);

        assertNull(normalizedUser.getPassword());
        assertEquals("13800138000", normalizedUser.getTel());
        assertEquals("110101199901011234", normalizedUser.getIdcard());
    }
}
