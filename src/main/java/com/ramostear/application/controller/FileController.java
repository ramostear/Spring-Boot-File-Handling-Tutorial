package com.ramostear.application.controller;

import com.ramostear.application.model.FileInfo;
import com.ramostear.application.util.FileUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : ramostear
 * @date : 2019/3/8 0008-15:35
 */
@Controller
public class FileController {

    private static String fileUploadRootDir = null;

    @Value ( "${file.upload.root.dir.windows}" )
    String fileUploadRootDirWindows;

    @Value ( "${file.upload.root.dir.mac}" )
    String fileUploadRootDirMac;

    @Value ( "${file.upload.root.dir.linux}" )
    String fileUploadRootDirLinux;

    private static Map<String,FileInfo> fileRepository = new HashMap<>();

    @PostConstruct
    public void initFileRepository(){
        FileInfo file1 = new FileInfo ().setFileName ( "bg1.jpg" );
        FileInfo file2 = new FileInfo ().setFileName ( "bg2.jpg" );
        FileInfo file3 = new FileInfo ().setFileName ( "bg3.jpg" );
        fileRepository.put ( file1.getName (),file1 );
        fileRepository.put ( file2.getName (),file2 );
        fileRepository.put ( file3.getName (),file3 );

        // 判断文件夹是否存在，不存在就创建
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Mac OS")) {
            // 苹果
            fileUploadRootDir = fileUploadRootDirMac;
        } else if (osName.startsWith("Windows")) {
            // windows
            fileUploadRootDir = fileUploadRootDirWindows;
        } else {
            // unix or linux
            fileUploadRootDir = fileUploadRootDirLinux;
        }
        FileUtil.createDirectories(fileUploadRootDir);
    }

    @GetMapping("/files")
    public String files(Model model){
        Collection<FileInfo> files = fileRepository.values ();
        model.addAttribute ( "data",files );
        return "files";
    }


    @PostMapping(value = "/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public String fileUpload(@RequestParam("file") MultipartFile file) throws IOException {

        File convertFile = new File ( fileUploadRootDir+file.getOriginalFilename ());
        FileOutputStream fileOutputStream = new FileOutputStream ( convertFile );
        fileOutputStream.write ( file.getBytes () );
        fileOutputStream.close ();

        FileInfo fileInfo = new FileInfo()
                .setFileName ( file.getOriginalFilename());

        fileRepository.put ( fileInfo.getName (),fileInfo);

        return "File is upload successfully";
    }

    @GetMapping("/download/{fileName}")
    @ResponseBody
    public ResponseEntity<Object> downloadFile(@PathVariable(name = "fileName") String fileName) throws FileNotFoundException, UnsupportedEncodingException {

        File file = new File ( fileUploadRootDir+fileName);
        InputStreamResource resource = new InputStreamResource ( new FileInputStream ( file ) );

        HttpHeaders headers = new HttpHeaders();
        // 使用URLEncoder.encode(fileName, "UTF-8") 下载文件能正常显示中文
        headers.add ( "Content-Disposition",String.format("attachment;filename=\"%s", URLEncoder.encode(fileName, "UTF-8")));
        headers.add ( "Cache-Control","no-cache,no-store,must-revalidate" );
        headers.add ( "Pragma","no-cache" );
        headers.add ( "Expires","0" );

        ResponseEntity<Object> responseEntity = ResponseEntity.ok()
                .headers ( headers )
                .contentLength ( file.length ())
                .contentType(MediaType.parseMediaType ( "application/txt" ))
                .body(resource);

        return responseEntity;
    }



}
