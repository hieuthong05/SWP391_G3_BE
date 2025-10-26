package BE.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
@RequestMapping("/api/upload")
public class ImageUploadController {
    private static final String IMAGE_BASE_PATH = "src/main/resources/static/images/";

    @PostMapping("/{folder}")
    public ResponseEntity<String> uploadImage(@PathVariable String folder,
                                              @RequestParam("file") MultipartFile file) {
        try {
            String folderPath = IMAGE_BASE_PATH + folder;
            File dir = new File(folderPath);
            if (!dir.exists()) dir.mkdirs();

            String fileName = file.getOriginalFilename();
            String fullPath = folderPath + "/" + fileName;
            file.transferTo(new File(fullPath));

            // Trả về đường dẫn để lưu vào DB
            String imageUrl = "/images/" + folder + "/" + fileName;
            return ResponseEntity.ok(imageUrl);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upload failed: " + e.getMessage());
        }
    }
}
