package com.backend.hallodos.services;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.backend.hallodos.model.FileDb;
import com.backend.hallodos.repository.FileDbRepository;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;



@Service
public class FileUploadService {
	FileDbRepository fileDbRepo;

	public void uploadFile(MultipartFile file) throws IllegalStateException, IOException {
		file.transferTo(new File("D:\\Bootcamp\\Hallodos\\user-photos"+file.getOriginalFilename()));
	}
	public FileDb store(MultipartFile file) throws IOException {
		String fileName = file.getOriginalFilename();
		FileDb fileDb = new FileDb(UUID.randomUUID().toString(),fileName,file.getContentType(),file.getBytes()) ;
		return fileDbRepo.save(fileDb);
	}
	public FileDb getFileById(String id) {
		Optional<FileDb> fileOptional = fileDbRepo.findById(id);
		
		if(fileOptional.isPresent()) {
			return fileOptional.get()
;		}
		return null;
	}
	public List<FileDb> getFileList(){	
	return fileDbRepo.findAll();
	}
	}

