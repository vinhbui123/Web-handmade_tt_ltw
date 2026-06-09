package vn.edu.hcmuaf.fit.Web_ban_hang.services;

import java.util.ArrayList;
import java.util.List;

import vn.edu.hcmuaf.fit.Web_ban_hang.dao.InventoryDao;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.OrderDao;
import vn.edu.hcmuaf.fit.Web_ban_hang.dao.dto.DetailOrderDTO;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Order;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.OrderDetail;

public class OrderService {

    private OrderDao orderDao = new OrderDao();

    public void addOrder(Order order, List<OrderDetail> orderDetails) {
        orderDao.addOrder(order, orderDetails);
    }
    public List<Order> getAllOrders(int userId) {
        return orderDao.getAllOrders(userId);
    }

    public List<OrderDetail> toDetailOrder(List<DetailOrderDTO> detailOrderDTOS) {
        List<OrderDetail> result = new ArrayList<>();
        for (DetailOrderDTO dto : detailOrderDTOS) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setProductId(dto.getProductId());
            orderDetail.setPrice(dto.getPrice());
            orderDetail.setQuantity(dto.getQuantity());
            orderDetail.setTotalMoney(dto.getPrice() * dto.getQuantity());
            orderDetail.setDiscountAmount(dto.getDiscountAmount());
            result.add(orderDetail);
        }
        return result;
    }

    public boolean CheckStock(int productId, int quantity) {
        InventoryDao inventoryDao = new InventoryDao();
        int stock = inventoryDao.getStock(productId);
        return stock < quantity;
    }

    public boolean cancelOrder(int orderId, int userId) {
        return orderDao.cancelOrder(orderId, userId);
    }

}
