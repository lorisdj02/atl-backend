package com.atl.backend.service.storage;

import com.atl.backend.model.Account;
import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {
    String storeProfilePicture(Account account, MultipartFile file);
}
