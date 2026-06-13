package vn.edu.hcmuaf.fit.Web_ban_hang.services;

import java.util.List;
import java.util.Set;

import vn.edu.hcmuaf.fit.Web_ban_hang.dao.WishlistDao;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Product;

public class WishlistService {
    private final WishlistDao wishlistDao = new WishlistDao();

    public boolean toggle(int userId, int productId) {
        if (wishlistDao.exists(userId, productId)) {
            wishlistDao.remove(userId, productId);
            return false; 
        } else {
            wishlistDao.add(userId, productId);
            return true; 
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
