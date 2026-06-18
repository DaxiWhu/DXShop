package com.daxi.controller;

import cn.hutool.core.collection.CollUtil;
import com.daxi.annotation.InternalApi;
import com.daxi.domain.ao.AddressModifyAuditAO;
import com.daxi.domain.ao.LogisticsCenterAO;
import com.daxi.domain.ao.OrderRefundAO;
import com.daxi.domain.ao.PaymentAO;
import com.daxi.domain.ao.RefundAuditAO;
import com.daxi.domain.ao.UserOrderAO;
import com.daxi.domain.ao.UserOrderAddressAO;
import com.daxi.domain.ao.UserSimpleOrderAO;
import com.daxi.domain.dto.AddressModifyRequestDTO;
import com.daxi.domain.dto.OrderStatusCountDTO;
import com.daxi.domain.dto.PaymentPageDTO;
import com.daxi.domain.dto.PaymentStatusDTO;
import com.daxi.domain.dto.RefundRequestDetailDTO;
import com.daxi.domain.dto.RefundRequestSimpleDTO;
import com.daxi.domain.dto.SendCommentDTO;
import com.daxi.domain.dto.UserOrderDTO;
import com.daxi.domain.dto.UserSimpleOrderDTO;
import com.daxi.result.Result;
import com.daxi.service.IOrderService;
import com.daxi.util.UserUtil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.daxi.response.CommonResponse.ID_ERROR;
import static com.daxi.response.CommonResponse.PARAM_EMPTY;
import static com.daxi.response.OrderResponse.ORDER_NOT_EXIST;
import static com.daxi.response.OrderResponse.ORDER_STATUS_ERROR;
import static com.daxi.response.UserResponse.NOT_LOGIN;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {
    private final @NonNull IOrderService orderService;
    public static final int MIN_ID_SCALE=1;
    /**user模块用于确定收获的接口*/
    @PutMapping("/api/ok")
    @InternalApi
    public UserOrderDTO getOrderForReceiveOrderAndOkStatus(
            @NotNull(message = PARAM_EMPTY)
            @Valid @Min(value = MIN_ID_SCALE, message = ID_ERROR)
            @RequestParam Long orderId,
            @NotNull(message = PARAM_EMPTY)
            @Valid @Min(value = MIN_ID_SCALE, message = ID_ERROR)
            @RequestParam Long userId){
        return orderService.getOrderForReceiveOrderAndOkStatus(orderId,userId);
    }
    /**取消订单1*/
    @DeleteMapping("/fail/{orderId}")
    public Result<Void> cancelOrder(
            @NotNull(message = PARAM_EMPTY)
            @Valid @Min(value = MIN_ID_SCALE, message = ID_ERROR)
            @PathVariable Long orderId){
        orderService.cancelOrder(orderId);
        return Result.success();
    }
    /**用户下单扣减库存并创建订单1*/
    @PostMapping()
    public Result<Void> createOrder(
            @NotNull
            @Valid
            @RequestBody UserOrderAO order){
        Long userId = UserUtil.getLocalUserId();
        if(userId==null){
            return Result.fail(NOT_LOGIN);
        }
        orderService.craeteOrder(order);
        return Result.success();
    }

    /**
     * 用户根据状态获取订单列表1
     * */

    @GetMapping ("/simple")
    public Result<List<UserSimpleOrderDTO>> getSimpleOrderByStatusForUser(
            @NotNull
            @Valid
            @RequestBody
            UserSimpleOrderAO order){
        Long userId=UserUtil.getLocalUserId();
        if(userId==null){
            return Result.fail(NOT_LOGIN);
        }
        List<UserSimpleOrderDTO> result = orderService
                .getSimpleOrderByStatusForUser(
                        userId,
                        order.getStatus(),
                        order.getPageSize(),
                        order.getPageNum());
        if(CollUtil.isEmpty( result)){
            return Result.fail(ORDER_STATUS_ERROR);
        }
        return Result.success(result);
    }
    /**商家根据状态获取订单列表1*/

    @GetMapping("/simple/shop")
    public Result<List<UserSimpleOrderDTO>> getSimpleOrderByStatusForShop(
            @NotNull
            @Valid
            @RequestBody
            UserSimpleOrderAO order){
        Long shopId = UserUtil.getLocalShopId();
        if(shopId==null){
            return Result.fail(NOT_LOGIN);
        }
        List<UserSimpleOrderDTO> result = orderService
                .getSimpleOrderByStatusForShop(
                        shopId,
                        order.getStatus(),
                        order.getPageSize(),
                        order.getPageNum());
        if(CollUtil.isEmpty(result)){
            return Result.fail(ORDER_STATUS_ERROR);
        }
        return Result.success(result);
    }


    /**进入订单页面的时候用户加载各个状态的订单个数（可以不显示）1
     * 必须要从数据库里面拿
     * */
    @GetMapping("/category-number/user")
    public Result<OrderStatusCountDTO> getOrderEveryStatusNumberForUser(){
        Long userId = UserUtil.getLocalUserId();
        if (userId==null){
            return Result.fail(NOT_LOGIN);
        }
        OrderStatusCountDTO orderEveryStatusNumber = orderService.getOrderEveryStatusNumberForUser(userId);
        if(orderEveryStatusNumber==null){
            return Result.fail(ORDER_STATUS_ERROR);
        }
        return Result.success();
    }
    /**进入订单页面的时候店铺加载各个状态的订单个数（可以不显示）1
     * 必须要从数据库里面拿
     * */
    @GetMapping("/category-number/shop")
    public Result<OrderStatusCountDTO> getOrderEveryStatusNumberForShop(){
        Long shopId = UserUtil.getLocalShopId();
        if (shopId==null){
            return Result.fail(NOT_LOGIN);
        }
        OrderStatusCountDTO orderEveryStatusNumber = orderService.getOrderEveryStatusNumberForShop(shopId);
        if(orderEveryStatusNumber==null){
            return Result.fail(ORDER_STATUS_ERROR);
        }
        return Result.success();
    }
    /**用户申请更换订单收货地址1*/

    @PutMapping("/address")
    public Result<Void> updateOrderAddress(
            @NotNull
            @Valid
            @RequestBody
            UserOrderAddressAO address){
        orderService.updateAddress(address);
        return Result.success();
    }
    /**用户申请退款1*/

    @PostMapping("/refunds")
    public Result<Void> sendRefunds(
            @NotNull
            @Valid
            @RequestBody
            OrderRefundAO refunds){
        orderService.sendRefunds(refunds);
        return Result.success();
    }

    /**拉取支付页面1*/
    @PostMapping("/payments/page")
    public Result<PaymentPageDTO> getPayPage(
            @NotNull(message = PARAM_EMPTY)
            @Valid @Min(value = MIN_ID_SCALE, message = ID_ERROR)Long orderId){
        PaymentPageDTO result = orderService.getPaymentPage(orderId);
        return Result.success(result);
    }
    /**提供给第三方支付服务异步调用的，在这里修改订单状态1*/
    @PostMapping("/payments/success")
    public Result<Void> updateOrderStatusToPaySuccess(
            @NotNull
            @Valid
            @RequestBody PaymentAO payment){
        orderService.updateOrderStatusToPaySuccess(payment);
        return Result.success();
    }
    /**确认支付状态1*/
    @PutMapping("/payments")
    public Result<PaymentStatusDTO> checkPaymentStatus(
            @NotNull(message = PARAM_EMPTY)
            @Valid @Min(value = MIN_ID_SCALE, message = ID_ERROR)Long orderId){
        PaymentStatusDTO result = orderService.checkPaymentStatus(orderId);
        return Result.success(result);
    }

    /**用户查看更换地址申请1*/
    @GetMapping("/address/requests/user")
    public Result<List<AddressModifyRequestDTO>> getAddressModifyRequestsForUser(
            @NotNull
            @Valid
            @RequestBody
            UserSimpleOrderAO request) {
        Long userId = UserUtil.getLocalUserId();
        if (userId == null) {
            return Result.fail(NOT_LOGIN);
        }
        List<AddressModifyRequestDTO> result = orderService.getAddressModifyRequestsForUser(
                userId,
                request.getStatus(),
                request.getPageNum(),
                request.getPageSize());
        if (CollUtil.isEmpty(result)) {
            return Result.fail(ORDER_STATUS_ERROR);
        }
        return Result.success(result);
    }

    /**用户查看退款等申请1*/
    @GetMapping("/refund/requests/user")
    public Result<List<RefundRequestSimpleDTO>> getRefundRequestsForUser(
            @NotNull
            @Valid
            @RequestBody
            UserSimpleOrderAO request) {
        Long userId = UserUtil.getLocalUserId();
        if (userId == null) {
            return Result.fail(NOT_LOGIN);
        }
        List<RefundRequestSimpleDTO> result = orderService.getRefundRequestsForUser(
                userId,
                request.getStatus(),
                request.getPageNum(),
                request.getPageSize());
        if (CollUtil.isEmpty(result)) {
            return Result.fail(ORDER_STATUS_ERROR);
        }
        return Result.success(result);
    }

    /**商家查看更换地址的申请1*/
    @GetMapping("/address/requests/shop")
    public Result<List<AddressModifyRequestDTO>> getAddressModifyRequestsForShop(
            @NotNull
            @Valid
            @RequestBody
            UserSimpleOrderAO request) {
        Long shopId = UserUtil.getLocalShopId();
        if (shopId == null) {
            return Result.fail(NOT_LOGIN);
        }
        List<AddressModifyRequestDTO> result = orderService.getAddressModifyRequestsForShop(
                shopId,
                request.getStatus(),
                request.getPageNum(),
                request.getPageSize());
        if (CollUtil.isEmpty(result)) {
            return Result.fail(ORDER_STATUS_ERROR);
        }
        return Result.success(result);
    }

    /**商家查看退款的申请1*/
    @GetMapping("/refund/requests/shop")
    public Result<List<RefundRequestSimpleDTO>> getRefundRequestsForShop(
            @NotNull
            @Valid
            @RequestBody
            UserSimpleOrderAO request) {
        Long shopId = UserUtil.getLocalShopId();
        if (shopId == null) {
            return Result.fail(NOT_LOGIN);
        }
        List<RefundRequestSimpleDTO> result = orderService.getRefundRequestsForShop(
                shopId,
                request.getStatus(),
                request.getPageNum(),
                request.getPageSize());
        if (CollUtil.isEmpty(result)) {
            return Result.fail(ORDER_STATUS_ERROR);
        }
        return Result.success(result);
    }





    /**用户根据订单id获取具体订单信息1*/
    @GetMapping ("/detail/{orderId}")
    public Result<UserOrderDTO> getDetailOrderById(
            @PathVariable
            @NotNull(message = PARAM_EMPTY)
            @Valid @Min(value = MIN_ID_SCALE, message = ID_ERROR) Long orderId){
        UserOrderDTO result = orderService.getDetailOrderById(orderId);
        if(result==null){
            return Result.fail(ORDER_NOT_EXIST);
        }
        return Result.success(result);
    }

     /**
      * 通过订单id查看退款的详细信息1
      * */

    @GetMapping("/refund/requests/{orderId}")
    public Result<RefundRequestDetailDTO> getRefundRequestDetailById(
            @PathVariable
            @NotNull(message = PARAM_EMPTY)
            @Valid @Min(value = MIN_ID_SCALE, message = ID_ERROR) Long orderId){
        RefundRequestDetailDTO result = orderService.getRefundRequestDetailById(orderId);
        if(result==null){
            return Result.fail(ORDER_NOT_EXIST);
        }
        return Result.success(result);
    }

    /**商家审核更换地址的申请1*/
    @PutMapping("/address/audit")
    public Result<Void> auditAddressModify(
            @NotNull
            @Valid
            @RequestBody AddressModifyAuditAO audit){
        orderService.auditAddressModify(audit);
        return Result.success();
    }
    /**
     * 商家审核退款需求，或者是收到退货之后认为不可以退款1
     * */
    @PutMapping("/refund/audit")
    public Result<Void> auditRefund(
            @NotNull
            @Valid
            @RequestBody RefundAuditAO audit){
        orderService.auditRefund(audit);
        return Result.success();
    }
    /**
     * 物流中心确认收到用户退的货1
     * */
    @PutMapping("/refund/user-ship")
    public Result<Void> userShipForRefund(
            @NotNull
            @Valid
            @RequestBody LogisticsCenterAO orderInfo){
        orderService.userShipForRefund(orderInfo.getRefundId(), orderInfo.getReturnExpressCompany(), orderInfo.getReturnWaybill());
        return Result.success();
    }
    /**用于发送评论的信息获取*/
    @GetMapping("/comment")
    SendCommentDTO getInformationForSendComment(
            @RequestParam Long userId,
            @RequestParam Long orderId,@RequestParam Long spuId,@RequestParam Long skuId){
        return orderService.getInformationForSendComment(userId,orderId,spuId,skuId);
    }
}


