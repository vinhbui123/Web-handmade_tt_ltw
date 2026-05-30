package vn.edu.hcmuaf.fit.Web_ban_hang.services;

import vn.edu.hcmuaf.fit.Web_ban_hang.dao.WishlistDao;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Product;

import java.util.List;
import java.util.Set;

public class WishlistService {
    private final WishlistDao wishlistDao = new WishlistDao();

    /**
     * Toggle wishlist: thêm nếu chưa có, xóa nếu đã có.
     * @return true nếu đã thêm, false nếu đã xóa
     */
    public boolean toggle(int userId, int productId) {
        if (wishlistDao.exists(userId, productId)) {
            wishlistDao.remove(userId, productId);
            return false; // đã xóa
        } else {
            wishlistDao.add(userId, productId);
            return true; // đã thêm
        }
    }

    public boolean add(int userId, int productId) {
        return wishlistDao.add(userId, productId);
    }

    public boolean remove(int userId, int productId) {
        return wishlistDao.remove(userId, productId);
    }

    public boolean exists(int userId, int productId) {
        return wishlistDao.exists(userId, productId);
    }

    public Set<Integer> getProductIdsByUserId(int userId) {
        return wishlistDao.getProductIdsByUserId(userId);
    }

    public List<Product> getProductsByUserId(int userId) {
        return wishlistDao.getProductsByUserId(userId);
    }

    public int countByUserId(int userId) {
        return wishlistDao.countByUserId(userId);
    }
}
