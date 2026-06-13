package vn.edu.hcmuaf.fit.Web_ban_hang.services;

import java.util.List;

import vn.edu.hcmuaf.fit.Web_ban_hang.dao.AddressDao;
import vn.edu.hcmuaf.fit.Web_ban_hang.model.Address;

public class AddressService {
    AddressDao addressDao = new AddressDao();

    public boolean insertAddressAndSetDefault(Address address) {return addressDao.insertAddressAndSetDefault(address);}

    public List<Address> getAddressByIdUser(int userId) {
        return addressDao.getAddressByIdUser(userId);
    }

    public boolean updateAddress(Address address) {
        return addressDao.updateAddress(address);
    }

    public boolean deleteAddress(int idAddress) {
        return addressDao.deleteAddress(idAddress);
    }

    public boolean setDefault(Address address) {
        return addressDao.setDefault(address);
    }

    public Address getAddressDefault(int userId) {
        return addressDao.getAddressDefault(userId);
    }

    public Address getAddressById(int id) {
        return addressDao.getAddressById(id);
    }
}
