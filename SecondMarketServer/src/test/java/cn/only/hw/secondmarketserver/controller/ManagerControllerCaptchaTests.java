package cn.only.hw.secondmarketserver.controller;

import cn.only.hw.secondmarketserver.entity.Manager;
import cn.only.hw.secondmarketserver.service.AddressService;
import cn.only.hw.secondmarketserver.service.BannerService;
import cn.only.hw.secondmarketserver.service.CatechildService;
import cn.only.hw.secondmarketserver.service.CategoryService;
import cn.only.hw.secondmarketserver.service.ForumService;
import cn.only.hw.secondmarketserver.service.GoodsService;
import cn.only.hw.secondmarketserver.service.ManagerService;
import cn.only.hw.secondmarketserver.service.MenuService;
import cn.only.hw.secondmarketserver.service.NoticeService;
import cn.only.hw.secondmarketserver.service.OrdersService;
import cn.only.hw.secondmarketserver.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ManagerController.class)
class ManagerControllerCaptchaTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ManagerService managerService;

    @MockBean
    private UserService userService;

    @MockBean
    private BannerService bannerService;

    @MockBean
    private NoticeService noticeService;

    @MockBean
    private MenuService menuService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private CatechildService catechildService;

    @MockBean
    private GoodsService goodsService;

    @MockBean
    private ForumService forumService;

    @MockBean
    private OrdersService ordersService;

    @MockBean
    private AddressService addressService;

    @Test
    void shouldGenerateCaptchaImageAndStoreCaptchaInSession() throws Exception {
        MockHttpSession session = new MockHttpSession();

        mockMvc.perform(get("/manager/captcha").session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"));

        Object captcha = session.getAttribute("managerCaptcha");
        assertNotNull(captcha);
        assertTrue(String.valueOf(captcha).matches("\\d{4}"));
    }

    @Test
    void shouldLoginWhenCaptchaMatches() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("managerCaptcha", "2468");

        Manager loginManager = new Manager();
        loginManager.setId(7);
        loginManager.setAccount("admin");
        loginManager.setPassword("123456");
        when(managerService.login(any(Manager.class))).thenReturn(loginManager);

        mockMvc.perform(post("/manager/login")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"account\":\" admin \",\"password\":\" 123456 \",\"captcha\":\"2468\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.id").value(7))
                .andExpect(jsonPath("$.data.account").value("admin"));

        ArgumentCaptor<Manager> managerCaptor = ArgumentCaptor.forClass(Manager.class);
        verify(managerService).login(managerCaptor.capture());
        assertEquals("admin", managerCaptor.getValue().getAccount());
        assertEquals("123456", managerCaptor.getValue().getPassword());
        assertEquals(7, session.getAttribute("user"));
        assertNull(session.getAttribute("managerCaptcha"));
    }

    @Test
    void shouldRejectLoginWhenCaptchaDoesNotMatch() throws Exception {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("managerCaptcha", "2468");

        mockMvc.perform(post("/manager/login")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"account\":\"admin\",\"password\":\"123456\",\"captcha\":\"0000\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("Captcha is incorrect"));

        verify(managerService, never()).login(any(Manager.class));
        assertNull(session.getAttribute("managerCaptcha"));
        assertNull(session.getAttribute("user"));
    }
}
