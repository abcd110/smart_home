package com.smarthome.common.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * CommonUtils测试类
 */
class CommonUtilsTest {

    @Test
    void testGenerateUUID() {
        String uuid = CommonUtils.generateUUID();
        assertNotNull(uuid);
        assertEquals(32, uuid.length());
        assertFalse(uuid.contains("-"));
    }

    @Test
    void testIsEmpty() {
        assertTrue(CommonUtils.isEmpty(null));
        assertTrue(CommonUtils.isEmpty(""));
        assertTrue(CommonUtils.isEmpty("   "));
        assertFalse(CommonUtils.isEmpty("test"));
    }

    @Test
    void testIsNotEmpty() {
        assertFalse(CommonUtils.isNotEmpty(null));
        assertFalse(CommonUtils.isNotEmpty(""));
        assertFalse(CommonUtils.isNotEmpty("   "));
        assertTrue(CommonUtils.isNotEmpty("test"));
    }

    @Test
    void testIsValidEmail() {
        assertTrue(CommonUtils.isValidEmail("test@example.com"));
        assertTrue(CommonUtils.isValidEmail("user.name@domain.co.uk"));
        assertFalse(CommonUtils.isValidEmail("invalid-email"));
        assertFalse(CommonUtils.isValidEmail(""));
        assertFalse(CommonUtils.isValidEmail(null));
    }

    @Test
    void testIsValidPhone() {
        assertTrue(CommonUtils.isValidPhone("13812345678"));
        assertTrue(CommonUtils.isValidPhone("13987654321"));
        assertFalse(CommonUtils.isValidPhone("1234567890"));
        assertFalse(CommonUtils.isValidPhone("138123456789"));
        assertFalse(CommonUtils.isValidPhone(""));
        assertFalse(CommonUtils.isValidPhone(null));
    }
}