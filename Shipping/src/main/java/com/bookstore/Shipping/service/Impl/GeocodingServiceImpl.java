package com.bookstore.Shipping.service.Impl;

import com.bookstore.Shipping.dto.MapboxGeocodingResponse;
import com.bookstore.Shipping.service.IGeocodingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer; // Dùng Normalizer để loại bỏ dấu
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class GeocodingServiceImpl implements IGeocodingService {

    @Value("${mapbox.access.token}")
    private String mapboxToken;

    private final RestTemplate restTemplate;

    public GeocodingServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    // HÀM LOẠI BỎ DẤU TIẾNG VIỆT (Sử dụng Normalizer API chuẩn của Java)
    public static String removeDiacritics(String text) {
        String nfdNormalizedString = Normalizer.normalize(text, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(nfdNormalizedString).replaceAll("").replace('Đ', 'D').replace('đ', 'd');
    }

// ---------------------------------------------------------------------
// LOGIC RÚT GỌN ĐỊA CHỈ (FINAL: CHUẨN HÓA VÀ LOẠI BỎ DẤU)
// ---------------------------------------------------------------------

    /**
     * Tối ưu hóa chuỗi địa chỉ: Loại bỏ dấu Tiếng Việt, loại bỏ từ đệm,
     * và chỉ giữ lại các thành phần quan trọng (Địa chỉ, Quận, TP).
     */
    private String simplifyAddress(String fullAddress) {
        if (fullAddress == null || fullAddress.isBlank() || "null".equalsIgnoreCase(fullAddress)) {
            return "";
        }

        // 1. CHUẨN HÓA CÁC TỪ KHÓA CẤP HÀNH CHÍNH
        String processedAddress = fullAddress.toUpperCase(Locale.ROOT)
                // Loại bỏ các từ KHÔNG cần thiết cho Geocoding
                .replace("VIỆT NAM", "")
                .replace("SỐ NHÀ", "")
                .replace("KHU PHỐ", "")
                .replace("ẤP", "")
                .replace("THÔN", "")
                .replace("XÓM", "")

                // RÚT GỌN CHUẨN (THÀNH PHỐ)
                .replace("THÀNH PHỐ", "TP")
                .replace("TỈNH", "")
                .replace("HỒ CHÍ MINH", "TPHCM")
                .replace("HÀ NỘI", "HANOI")

                // LOẠI BỎ CÁC TỪ CẤP HÀNH CHÍNH (ĐƯỜNG, PHƯỜNG, QUẬN) để giảm Token Count
                .replace("ĐƯỜNG", "")
                .replace("PHƯỜNG", "")
                .replace("QUẬN", "")
                .replace("HUYỆN", "")

                // Loại bỏ ký tự đặc biệt và chuẩn hóa khoảng trắng
                .replace(",", " ")
                .replace(".", " ")
                .replace("/", " ")
                .replace("-", " ")
                .trim()
                .replaceAll("\\s+", " "); // Chuẩn hóa khoảng trắng thừa

        // 2. LOẠI BỎ DẤU TIẾNG VIỆT VÀ CHUẨN HÓA LẠI
        processedAddress = removeDiacritics(processedAddress).toUpperCase(Locale.ROOT);

        // Bổ sung các từ ngữ cảnh còn thiếu sau khi loại bỏ dấu (Ví dụ: Thêm lại QUAN GÒ VẤP)
        if (!processedAddress.contains("TPHCM")) {
            // Giả định là TP.HCM nếu không có thành phố khác
            processedAddress += " TPHCM";
        }

        // Nếu địa chỉ thiếu GÒ VẤP, thêm GÒ VAP (không dấu)
        if (processedAddress.contains("LE VAN THO") && !processedAddress.contains("GO VAP")) {
            processedAddress += " GO VAP";
        }


        // 3. GIỚI HẠN TOKEN CUỐI CÙNG (SAU KHI ĐÃ LÀM SẠCH)
        String[] tokens = processedAddress.split("\\s+");
        List<String> tokenList = Arrays.asList(tokens);

        // Giữ giới hạn tối đa là 16 token để đảm bảo an toàn sau khi URL encoding
        final int MAX_FINAL_TOKENS = 16;

        if (tokenList.size() > MAX_FINAL_TOKENS) {
            processedAddress = tokenList.subList(0, MAX_FINAL_TOKENS).stream().collect(Collectors.joining(" "));
        } else {
            processedAddress = tokenList.stream().collect(Collectors.joining(" "));
        }

        processedAddress = processedAddress.trim();
        System.out.println("Address simplified to (FINAL " + processedAddress.split("\\s+").length + " tokens): " + processedAddress);

        return processedAddress;
    }

// ---------------------------------------------------------------------
// PHƯƠNG THỨC CHÍNH (GIỮ NGUYÊN)
// ---------------------------------------------------------------------

    @Override
    public double[] getCoordinatesFromAddress(String address) {
        // ... (Phần code này giữ nguyên)
        String simplifiedAddress = simplifyAddress(address);

        if (simplifiedAddress.isEmpty()) {
            System.err.println("Geocoding failed: Address is empty after simplification.");
            return new double[]{0, 0};
        }

        try {
            // 2. URL encoding
            String encoded = URLEncoder.encode(simplifiedAddress, StandardCharsets.UTF_8);

            // 3. Xây dựng URL API VỚI THAM SỐ country=vn
            String url = String.format(
                    "https://api.mapbox.com/geocoding/v5/mapbox.places/%s.json?limit=1&country=vn&access_token=%s",
                    encoded, mapboxToken
            );

            MapboxGeocodingResponse response =
                    restTemplate.getForObject(url, MapboxGeocodingResponse.class);

            if (response != null && response.getFeatures() != null && !response.getFeatures().isEmpty()) {
                double lon = response.getFeatures().get(0).getCenter().get(0); // Mapbox: [lon, lat]
                double lat = response.getFeatures().get(0).getCenter().get(1);

                System.out.printf(Locale.US, "Geocoded (simplified) [%s] -> lon: %.6f, lat: %.6f%n", simplifiedAddress, lon, lat);

                return new double[]{lon, lat};
            }

            System.err.println("Geocoding failed: No features found for simplified address: " + simplifiedAddress);
            return new double[]{0, 0};

        } catch (HttpClientErrorException e) {
            System.err.println("Mapbox API Error (Status " + e.getRawStatusCode() + ") for simplified address: " + simplifiedAddress + ". Error: " + e.getResponseBodyAsString());
            return new double[]{0, 0};
        } catch (Exception e) {
            e.printStackTrace();
            return new double[]{0, 0};
        }
    }
}