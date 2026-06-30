package cn.only.hw.secondmarketserver.controller;

import cn.only.hw.secondmarketserver.entity.Goods;
import cn.only.hw.secondmarketserver.entity.Orders;
import cn.only.hw.secondmarketserver.entity.User;
import cn.only.hw.secondmarketserver.service.GoodsService;
import cn.only.hw.secondmarketserver.service.OrdersService;
import cn.only.hw.secondmarketserver.service.UserService;
import cn.only.hw.secondmarketserver.util.Result;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private GoodsService goodsService;

    @MockBean
    private OrdersService ordersService;

    @Test
    void shouldRejectRegisterWhenNicknameIsMissing() throws Exception {
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"
                                + "\"account\":\"student001\","
                                + "\"password\":\"password123\","
                                + "\"sex\":\"男\","
                                + "\"tel\":\"13800138000\","
                                + "\"idcard\":\"11010519491231002X\","
                                + "\"college\":\"计算机学院\","
                                + "\"grade\":\"2022级\""
                                + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.msg").value("nickname is empty"));

        verify(userService, never()).register(any(User.class));
    }

    @Test
    void shouldAllowEmptyRoomnumbWhenOtherFieldsArePresent() throws Exception {
        when(userService.register(any(User.class))).thenReturn(Result.success("注册成功"));

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"
                                + "\"account\":\" student001 \","
                                + "\"password\":\" password123 \","
                                + "\"nickname\":\" 张三 \","
                                + "\"sex\":\" 男 \","
                                + "\"tel\":\" 13800138000 \","
                                + "\"idcard\":\" 11010519491231002x \","
                                + "\"college\":\" 计算机学院 \","
                                + "\"grade\":\" 2022级 \","
                                + "\"roomnumb\":\"   \""
                                + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data").value("注册成功"));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userService).register(captor.capture());
        assertEquals("student001", captor.getValue().getAccount());
        assertEquals("password123", captor.getValue().getPassword());
        assertEquals("张三", captor.getValue().getNickname());
        assertEquals("男", captor.getValue().getSex());
        assertEquals("13800138000", captor.getValue().getTel());
        assertEquals("11010519491231002x", captor.getValue().getIdcard());
        assertEquals("计算机学院", captor.getValue().getCollege());
        assertEquals("2022级", captor.getValue().getGrade());
        assertEquals("", captor.getValue().getRoomnumb());
    }
}
