package com.poly.ASM.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class CameraAController {

    @Value("${camera.hls-url:http://localhost:8888/camera/index.m3u8}")
    private String cameraHlsUrl;

    @GetMapping("/admin/camera/index")
    public String index(Model model) {
        model.addAttribute("cameraStreamUrl", cameraHlsUrl);
        return "admin/camera";
    }
}