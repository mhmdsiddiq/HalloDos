package com.backend.hallodos.controller;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
// import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import com.backend.hallodos.config.FileUploadUtil;
import com.backend.hallodos.dto.SignInDto;
import com.backend.hallodos.dto.SignupDto;
import com.backend.hallodos.exceptions.AuthFailException;
import com.backend.hallodos.exceptions.CustomExceptoon;
import com.backend.hallodos.model.AuthToken;
import com.backend.hallodos.model.Dosen;
import com.backend.hallodos.model.Mahasiswa;
import com.backend.hallodos.repository.DosenRepository;
import com.backend.hallodos.repository.MahasiswaRepository;
import com.backend.hallodos.services.AuthService;
import com.backend.hallodos.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class MahasiswaController {
	@Autowired
	DosenRepository dosenRepo;

	@Autowired
	AuthService authService;

	@Autowired
	MahasiswaRepository mahasiswaRepo;

	// get untuk memunculkan profil mahasiswa
	@GetMapping("/profilemahasiswa")
	public String Mahasiswa(Model model) {
		// model.addAttribute("fotoUser",new Mahasiswa());
		List<Mahasiswa> profil = mahasiswaRepo.findByStatus("ON");
		model.addAttribute("data", profil);
		return ("profilMaha");
	}

	@PostMapping("/mahasiswa/save")
	public RedirectView saveUser(Mahasiswa mahasiswa,
			@RequestParam(value = "image") MultipartFile multipartFile) throws IOException {

		String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());

		mahasiswa.setPhotos(fileName);

		Mahasiswa saveMaha = mahasiswaRepo.save(mahasiswa);

		String uploadDir = "user-photos/" + saveMaha.getUsername();

		FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);

		return new RedirectView("/profilemahasiswa", true);
	}

	// Tampilan awal dasboard awal mahasiswa
	@GetMapping("/dasboard")
	public String getdasboard(Model model, Principal principal) {
		return "index";
	}

	// untuk login mahasiswa
	@GetMapping("/loginMahasiswa")
	public String getIndex(Model model) {
		model.addAttribute("loginData", new Mahasiswa());
		return "loginMahasiswa";
	}

	// setelah masuk login, ini untuk menerima data dari login
	@PostMapping("/afterLogin")
	public String masukk(@ModelAttribute("loginData") Mahasiswa mahasiswa, Model model) {
		Mahasiswa maha = mahasiswaRepo.findByEmail_mahasiswa(mahasiswa.getEmail_mahasiswa());
		if (Objects.isNull(maha)) {
			return "kenihilan";
		}
		// hash the pass
		try {
			if (!maha.getPassword().equals(UserService.hashPassword(mahasiswa.getPassword()))) {
				throw new AuthFailException("wrong password!");
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		// compare pass in DB

		// if pass match
		AuthToken token = authService.getToken(maha);

		// retrive token
		if (Objects.isNull(token)) {
			throw new CustomExceptoon("token is not present!");
		}
		model.addAttribute("loginData", maha);
		return "dashboarduser";
	}

	@GetMapping("/dashboarduser")
	public String getDashboardDosen(@ModelAttribute("loginData") Mahasiswa mahasiswa, Dosen dosen, Model model) {
		Mahasiswa maha = mahasiswaRepo.findByEmail_mahasiswa(mahasiswa.getEmail_mahasiswa());
		Dosen topDos = dosenRepo.findByRating();
		if (maha == null) {
			return "kenihilan";
		} else {
			model.addAttribute("loginData", maha);
			model.addAttribute("dataTopDos", topDos);
			return "dashboarduser";
		}
	}

	@PostMapping("/afterDashboardMahasiswa")
	public String afterDashboardMahasiswa(@ModelAttribute("loginData") Mahasiswa mahasiswa, Model model) {
		Mahasiswa maha = mahasiswaRepo.findByEmail_mahasiswa(mahasiswa.getEmail_mahasiswa());
		if (Objects.isNull(maha)) {
			return "kenihilan";
		} else {
			model.addAttribute("loginData", maha);
			return "profilmahasiswa";
		}

	}

	// untuk forgot
	@GetMapping("/forgotMahasiswa")
	public String getforgot(Model model) {
		model.addAttribute("forgotData", new Mahasiswa());
		return "forgotMahasiswa";
	}

	// ini untuk cari email dan menerima data email, untuk mencari data security
	// question dan memunculkannya
	@PostMapping("/cariEmail")
	public String cariEmail(@ModelAttribute("forgotData") Mahasiswa mahasiswa, Model model) {
		String mhsemail = mahasiswa.getEmail_mahasiswa();
		Mahasiswa user = mahasiswaRepo.findBySecQuest(mhsemail);
		if (user == null) {
			// error 404
			return "kenihilan";
		} else {
			user.setSecurity_answer("");
			model.addAttribute("datamhs", user);
			return "qSecMahas";
		}

		// ini menerima jawaban dari security question dari mahasiswa
	}

	@PostMapping("/securityResult")
	public String secResult(@ModelAttribute("datamhs") Mahasiswa mahasiswa, Model model) {
		String mhsemail = mahasiswa.getEmail_mahasiswa();
		// Mahasiswa user = mahasiswaRepo.findBySecQuest(mhsemail);
		// String questionUser = mahasiswa.getSequrity_question();
		String answerUser = mahasiswa.getSecurity_answer();
		Mahasiswa result = mahasiswaRepo.findAnswerbyInputan(answerUser, mhsemail);
		if (result == null) {
			return "kenihilan";
		} else {
			model.addAttribute("newDataPassword", result);
			model.addAttribute("dataForgot", result);
			return "nPasswordMaha";
		}
	}

	// setelah menerima jawaban dari mahasiswa, form ini untuk memasukan form untuk
	// membuat password baru
	@PostMapping("/newPassword")
	public String newPassword(@ModelAttribute("newDataPassword") Mahasiswa mahasiswa, SignInDto signupDto,
			Model model) {
		Mahasiswa user = mahasiswaRepo.findByEmail_mahasiswa(mahasiswa.getEmail_mahasiswa());
		// String mhsemail = mahasiswa.getEmail_mahasiswa();

		String encryptedpassword = mahasiswa.getPassword();
		try {
			encryptedpassword = UserService.hashPassword(mahasiswa.getPassword());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		user.setPassword(encryptedpassword);

		mahasiswaRepo.save(user);
		// model.addAttribute("PasswordBaru", mahasiswa);
		return "redirect:/loginMahasiswa";
	}

	// Register Start mahasiswa

	@GetMapping("/registerMahasiswa")
	public String register(Model model) {
		model.addAttribute("data", new Mahasiswa());
		return "registerMahasiswa";
	}

	@PostMapping("/afterRegisterMaha")
	public String daftar(@ModelAttribute("data") SignupDto signupDto, Mahasiswa maha, Model model) {
		Dosen dosen = dosenRepo.findByEmail_dosen2(maha.getEmail_mahasiswa());
		// check if user is already
		if (Objects.nonNull(mahasiswaRepo.findByEmail_mahasiswa(maha.getEmail_mahasiswa())) || dosen != null) {
			throw new CustomExceptoon("User Already Present");
		}
		// hash the password
		String encryptedpassword = signupDto.getPassword();

		try {
			encryptedpassword = UserService.hashPassword(signupDto.getPassword());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		Mahasiswa user = new Mahasiswa(
				signupDto.getUsername(),
				encryptedpassword, null,
				maha.getSecurity_question(),
				maha.getSecurity_answer(), null, null,
				maha.getEmail_mahasiswa(),
				null, null, null, null, null);

		mahasiswaRepo.save(user);

		// create token
		final AuthToken authToken = new AuthToken(user);
		authService.saveConfirmationToken(authToken);

		model.addAttribute("loginData", new Mahasiswa());
		return "loginMahasiswa";
	}

	@GetMapping("/data")
	public String getData() {
		return "index";
	}

	@GetMapping("/getuser")
	public String getUser(Model model) {
		return "user";

	}
}
