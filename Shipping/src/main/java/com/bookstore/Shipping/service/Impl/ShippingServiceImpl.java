package com.bookstore.Shipping.service.Impl;

import com.bookstore.Shipping.dto.ShippingDto;
import com.bookstore.Shipping.entity.DeliveryUnit;
import com.bookstore.Shipping.entity.LocationPoint;
import com.bookstore.Shipping.entity.Shipping;
import com.bookstore.Shipping.entity.ShipInfor;
import com.bookstore.Shipping.repository.DeliveryUnitRepository;
import com.bookstore.Shipping.repository.ShippingRepository;
import com.bookstore.Shipping.repository.ShipInforRepository;
import com.bookstore.Shipping.service.IShippingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder; // Import cần thiết
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value; // ✅ Cần import này
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate; // ✅ Cần import này

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements IShippingService {

    private final ShippingRepository shippingRepository;
    private final ShipInforRepository shipInforRepository;
    private final DeliveryUnitRepository deliveryUnitRepository;

    // THÊM: Inject PasswordEncoder để mã hóa mật khẩu
    private final PasswordEncoder passwordEncoder;

    private final RestTemplate restTemplate; // ✅ Đã inject RestTemplate

    @Value("${api.order.url}")
    private String API_ORDER_URL;

    @Override
    public Shipping createAccount(ShippingDto dto) {
        if (shippingRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists: " + dto.getEmail());
        }

        String role = dto.getRole();
        if (role == null || (!role.equals("Shipper") && !role.equals("DeliveryUnit"))) {
            throw new RuntimeException("Invalid or missing role. Role must be 'Shipper' or 'DeliveryUnit'.");
        }

        // 1. Tạo account
        Shipping account = new Shipping();
        account.setEmail(dto.getEmail());

        // Mã hóa mật khẩu trước khi lưu
        String encodedPassword = passwordEncoder.encode(dto.getPassword());
        account.setPassword(encodedPassword);

        account.setRole(role);
        account.setStatus("Active");

        Shipping savedAccount = shippingRepository.save(account);

        // 2. Tạo thông tin chi tiết tương ứng dựa trên role
        if ("Shipper".equals(role)) {
            // Tạo ShipInfor mặc định
            ShipInfor shipInfor = new ShipInfor();
            shipInfor.setShipperId(savedAccount.getAccountId()); // lấy id từ Shipping
            shipInfor.setName("null");
            shipInfor.setPhone("null");
            shipInfor.setAddress("null");
            shipInfor.setAvatar("null");

            shipInforRepository.save(shipInfor);
        } else if ("DeliveryUnit".equals(role)) {
            DeliveryUnit unit = new DeliveryUnit();
            unit.setDeliveryUnitId(savedAccount.getAccountId()); // ✅ gán accountId
            unit.setName(savedAccount.getEmail());
            unit.setEmail(savedAccount.getEmail());
            unit.setPhone("null");
            unit.setBranchAddress("null");
            unit.setUnit("null");

            deliveryUnitRepository.save(unit);
        }


        return savedAccount;
    }

    @Override
    public List<Shipping> getAllShippersOnly() {
        return shippingRepository.findAll()
                .stream()
                .filter(s -> "Shipper".equals(s.getRole()))
                .toList();
    }

    @Override
    public List<Shipping> getAllDeliveryUnits() {
        return shippingRepository.findAll()
                .stream()
                .filter(s -> "DeliveryUnit".equals(s.getRole()))
                .toList();
    }

    @Override
    public Optional<String> getAddressByDeliveryUnitId(String deliveryUnitId) {
        Optional<DeliveryUnit> unitOptional = deliveryUnitRepository.findByDeliveryUnitId(deliveryUnitId);
        return unitOptional.map(DeliveryUnit::getBranchAddress);
    }

    @Override
    public Optional<String> getCustomerAddressByOrderId(String orderId) {
        try {
            // Endpoint: GET http://localhost:8082/api/orders/{orderId}/address
            String url = API_ORDER_URL + "/" + orderId + "/address";

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return Optional.of(response.getBody());
            } else {
                return Optional.empty();
            }
        } catch (HttpClientErrorException.NotFound e) {
            System.err.println("Không tìm thấy đơn hàng hoặc địa chỉ cho ID: " + orderId);
            return Optional.empty();
        } catch (Exception e) {
            System.err.println("Lỗi gọi Order Service để lấy địa chỉ cho ĐH " + orderId + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<DeliveryUnit> updateDeliveryUnit(String deliveryUnitId, DeliveryUnit updatedUnit) {
        Optional<DeliveryUnit> existingOptional = deliveryUnitRepository.findByDeliveryUnitId(deliveryUnitId);

        if (existingOptional.isEmpty()) {
            return Optional.empty();
        }

        DeliveryUnit existing = existingOptional.get();

        // Cập nhật các trường cho phép chỉnh sửa
        if (updatedUnit.getName() != null) {
            existing.setName(updatedUnit.getName());
        }
        if (updatedUnit.getEmail() != null) {
            existing.setEmail(updatedUnit.getEmail());
        }
        if (updatedUnit.getPhone() != null) {
            existing.setPhone(updatedUnit.getPhone());
        }
        if (updatedUnit.getBranchAddress() != null) {
            existing.setBranchAddress(updatedUnit.getBranchAddress());
        }

        DeliveryUnit saved = deliveryUnitRepository.save(existing);
        return Optional.of(saved);
    }

    @Override
    public Optional<DeliveryUnit> getDeliveryUnitById(String deliveryUnitId) {
        return deliveryUnitRepository.findByDeliveryUnitId(deliveryUnitId);
    }

    @Override
    public Optional<ShipInfor> getShipInforByShipperId(String shipperId) {
        return shipInforRepository.findByShipperId(shipperId);
    }

    @Override
    public List<DeliveryUnit> getDeliveryUnitsByUnit(String unit) {
        return deliveryUnitRepository.findByUnit(unit);
    }

    @Override
    public Optional<ShipInfor> updateShipInfor(String shipperId, ShipInfor updatedInfo) {
        return shipInforRepository.findByShipperId(shipperId).map(existing -> {
            existing.setName(updatedInfo.getName());
            existing.setPhone(updatedInfo.getPhone());
            existing.setAddress(updatedInfo.getAddress());
            existing.setAvatar(updatedInfo.getAvatar());
            existing.setLatitude(updatedInfo.getLatitude());
            existing.setLongitude(updatedInfo.getLongitude());
            existing.setLastUpdated(LocalDateTime.now());
            existing.setRouteHistory(updatedInfo.getRouteHistory());

            // ✅ CẬP NHẬT TRƯỜNG GEOJSONPOINT
            Double newLon = updatedInfo.getLongitude();
            Double newLat = updatedInfo.getLatitude();
            if (newLon != null && newLat != null) {
                existing.setCurrentLocation(new GeoJsonPoint(newLon, newLat));
            } else {
                existing.setCurrentLocation(null);
            }

            return shipInforRepository.save(existing);
        });
    }
}
