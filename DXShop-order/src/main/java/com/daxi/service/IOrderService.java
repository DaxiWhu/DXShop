package com.daxi.service;


import com.daxi.domain.ao.AddressModifyAuditAO;
import com.daxi.domain.ao.OrderRefundAO;
import com.daxi.domain.ao.PaymentAO;
import com.daxi.domain.ao.RefundAuditAO;
import com.daxi.domain.ao.UserOrderAO;
import com.daxi.domain.ao.UserOrderAddressAO;
import com.daxi.domain.dto.AddressModifyRequestDTO;
import com.daxi.domain.dto.OrderStatusCountDTO;
import com.daxi.domain.dto.RefundRequestDetailDTO;
import com.daxi.domain.dto.RefundRequestSimpleDTO;
import com.daxi.domain.dto.SendCommentDTO;
import com.daxi.domain.dto.UserOrderDTO;
import com.daxi.domain.dto.UserSimpleOrderDTO;

import java.util.List;

public interface IOrderService {
    void craeteOrder(UserOrderAO order);


    List<UserSimpleOrderDTO> getSimpleOrderByStatusForUser(Long userId, Integer status, Integer pageNum, Integer pageSize);

    List<UserSimpleOrderDTO> getSimpleOrderByStatusForShop(Long shopId, Integer status, Integer pageNum, Integer pageSize);

    OrderStatusCountDTO getOrderEveryStatusNumberForUser(Long userId);

    void updateAddress(UserOrderAddressAO address);

    void sendRefunds(OrderRefundAO refunds);

    void auditRefund(RefundAuditAO audit);

    void auditAddressModify(AddressModifyAuditAO audit);

    void userShipForRefund(Long refundId, String expressCompany, String waybill);


    UserOrderDTO getDetailOrderById(Long orderId);

    UserOrderDTO getOrderForReceiveOrderAndOkStatus(Long orderId, Long userId);

    void cancelOrder(Long orderId);

    OrderStatusCountDTO getOrderEveryStatusNumberForShop(Long shopId);

    List<AddressModifyRequestDTO> getAddressModifyRequestsForUser(Long userId, Integer status, Integer pageNum, Integer pageSize);

    List<RefundRequestSimpleDTO> getRefundRequestsForUser(Long userId, Integer status, Integer pageNum, Integer pageSize);

    List<AddressModifyRequestDTO> getAddressModifyRequestsForShop(Long shopId, Integer status, Integer pageNum, Integer pageSize);

    List<RefundRequestSimpleDTO> getRefundRequestsForShop(Long shopId, Integer status, Integer pageNum, Integer pageSize);

    RefundRequestDetailDTO getRefundRequestDetailById(Long orderId);

    com.daxi.domain.dto.PaymentPageDTO getPaymentPage(Long orderId);

    com.daxi.domain.dto.PaymentStatusDTO checkPaymentStatus(Long orderId);

    void updateOrderStatusToPaySuccess(PaymentAO pay);

    SendCommentDTO getInformationForSendComment(Long userId, Long orderId, Long spuId, Long skuId);
}
