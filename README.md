# Spring Boot（十一）— 文件上传和下载

文件上传和下载是Web应用程序比较常用的功能之一，在本章节中，我将以一个简单的案例来讲解在Spring Boot中如何进行文件的上传与下载。在开始正文之前，我们通过一张思维导图来了解一下文件上传与下载的简单流程：

![文件上传与下载思维导图](https://cdn.ramostear.com/2019-03-08-18-22-31-780bd13186fc406db8212e9e87e65d66.png "文件上传与下载思维导图")

# 1. 文件上传

对于文件上传，控制器中对应的上传方法的参数必须是**MultipartFile**对象，**MultipartFile**对象可以是一个数组对象，也可以是单个对象，如果是一个数组对象，则可以进行多文件上传；这里我们仅演示单个文件上传，下面的代码展示了文件上传方法的基本结构：

```java
@PostMapping(value = "/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
@ResponseBody
public String fileUpload(@RequestParam("file") MultipartFile file) throws IOException {
	return null;
}
```

接下来，我们使用**FileOutputStream**对象将客户端上传的文件写入到磁盘中，并返回**“File is upload successfully”**的提示信息，下面是文件上传完整的代码：

```java
package com.ramostear.application.controller;

import com.ramostear.application.model.FileInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : ramostear
 * @date : 2019/3/8 0008-15:35
 */
@Controller
public class FileController {


    @Value ( "${file.upload.root.dir}" )
    String fileUploadRootDir;

    private static Map<String,FileInfo> fileRepository = new HashMap<>();

    @PostConstruct
    public void initFileRepository(){
        FileInfo file1 = new FileInfo ().setFileName ( "bg1.jpg" );
        FileInfo file2 = new FileInfo ().setFileName ( "bg2.jpg" );
        FileInfo file3 = new FileInfo ().setFileName ( "bg3.jpg" );
        fileRepository.put ( file1.getName (),file1 );
        fileRepository.put ( file2.getName (),file2 );
        fileRepository.put ( file3.getName (),file3 );
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
}

```

**fileRepository**用于存放已上传文件的索引信息。



# 2. 文件下载

在Spring Boot应用程序中，我们可以使用**InputStreamResource**对象来下载文件，在下载文件的方法中，我们需要通过Response来设置HttpHeander对象的相关属性，如**Content-Disposition**、**Cache-Control**、**Pragma**和**Expires**等属性。除此之外，还需要指定Response的响应类型。下面的代码给出了文件下载的详细信息：

```java
@GetMapping("/download/{fileName}")
@ResponseBody
public ResponseEntity<Object> downloadFile(@PathVariable(name = "fileName") String fileName) throws FileNotFoundException {

        File file = new File ( fileUploadRootDir+fileName);
        InputStreamResource resource = new InputStreamResource ( new FileInputStream ( file ) );

        HttpHeaders headers = new HttpHeaders();
        headers.add ( "Content-Disposition",String.format("attachment;filename=\"%s",fileName));
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
```



# 3. 代码清单

## 3.1 文件上传和下载控制器

下面给出的是完整的文件上传和下载的代码：

```java
package com.ramostear.application.controller;

import com.ramostear.application.model.FileInfo;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : ramostear
 * @date : 2019/3/8 0008-15:35
 */
@Controller
public class FileController {


    @Value ( "${file.upload.root.dir}" )
    String fileUploadRootDir;

    private static Map<String,FileInfo> fileRepository = new HashMap<>();

    @PostConstruct
    public void initFileRepository(){
        FileInfo file1 = new FileInfo ().setFileName ( "bg1.jpg" );
        FileInfo file2 = new FileInfo ().setFileName ( "bg2.jpg" );
        FileInfo file3 = new FileInfo ().setFileName ( "bg3.jpg" );
        fileRepository.put ( file1.getName (),file1 );
        fileRepository.put ( file2.getName (),file2 );
        fileRepository.put ( file3.getName (),file3 );
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
    public ResponseEntity<Object> downloadFile(@PathVariable(name = "fileName") String fileName) throws FileNotFoundException {

        File file = new File ( fileUploadRootDir+fileName);
        InputStreamResource resource = new InputStreamResource ( new FileInputStream ( file ) );

        HttpHeaders headers = new HttpHeaders();
        headers.add ( "Content-Disposition",String.format("attachment;filename=\"%s",fileName));
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

```

## 3.2 数据模型

创建一个文件信息数据模型作为上传文件信息的载体，下面是FileInfo.java的代码：

```java
package com.ramostear.application.model;

import lombok.Data;

import java.util.Date;

/**
 * @author : ramostear
 * @date  : 2019/3/8 0008-15:25
 */
@Data
public class FileInfo {

    private String name;
    private Date uploadTime = new Date();

    public FileInfo setFileName(String name){
        this.setName ( name );
        return this;
    }

}

```



## 3.3 Maven build 文件

下面是本次demo应用程序的pom.xml文件配置清单：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.1.3.RELEASE</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.ramostear</groupId>
	<artifactId>file-handling</artifactId>
	<version>0.0.1-SNAPSHOT</version>
	<name>file-handling</name>
	<description>Demo project for Spring Boot</description>

	<properties>
		<java.version>1.8</java.version>
	</properties>

	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-thymeleaf</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-freemarker</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>

		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<optional>true</optional>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>

</project>

```

> 注：本次案例使用freemarker模板引擎作为视图模板



## 3.4 配置文件

application.properties文件主要设置了**freemarker**的相关属性以及自定义的**file.upload.root.dir **属性:

```properties
spring.freemarker.cache=false
spring.freemarker.prefix=
spring.freemarker.suffix=.html
spring.freemarker.enabled=true
spring.freemarker.charset=UTF-8
spring.freemarker.template-loader-path=classpath:/templates/
file.upload.root.dir = C:/work/upload/
```

**file.upload.root.dir**自定义属性设置了文件上传的更目录为：**C:/work/upload/**





## 3.5 视图

在视图文件中，创建了一个form表单用于上传文件，另外还创建了一个已上传文件列表，提供文件下载操作。

文件上传表单：

![上传文件form表单](https://cdn.ramostear.com/2019-03-08-18-23-54-30c336bab2dc4af3a4a53a949dfecc6e.png "上传文件form表单")

文件下载列表：

![文件下载列表](https://cdn.ramostear.com/2019-03-08-18-24-16-2acd5e38bc4247e098805173d8fa46c5.png "文件下载列表")

> 说明：文件上使用的是异步上传方式进行上传，没有使用同步提交form表单的方式进行

文件上传异步操作代码如下：

```javascript
$("#upload").on("click",function () {
           var fileObj = document.getElementById("file").files[0];
           var form = new FormData();
           form.append("file",fileObj);
           var xhr = new XMLHttpRequest();
           xhr.open("post","http://localhost:8080/upload",true);
           xhr.onload = function(event){
               alert(event.currentTarget.responseText);
               window.location.href = window.location.href;
           };
           xhr.send(form);
        });
```



# 4. 打包运行

使用Maven命令对应用程序进行打包，下面是maven打包的命令：

```tex
mvn clean install
```

在控制台窗口中运行上述命令，等待maven打包。若控制台中显示**“BUILD SUCCESS”**信息，你可以在当前工程目录下的target文件夹中找到相应的JAR文件。

现在，你可以使用下面的命令来运行JAR文件：

```tex
java -jar YOUR_JARFILE_NAME
```

JAR文件成功启动后，你可以在控制台窗口中看到如下的信息：

![控制台窗口信息](https://cdn.ramostear.com/2019-03-08-18-24-45-b299776efd5a4c6ba1ba4c3757703622.png "控制台窗口信息")



# 5. 测试

打开浏览器并在地址栏输入：http://localhost:8080/files 。下面是成功请求后的浏览器截图：
![文件列表](https://cdn.ramostear.com/2019-03-08-18-25-13-891de6f45dcc48059eb10f4cdb06a8eb.png "文件列表")


接下来，点击其中任意一个**download**按钮，测试文件下载功能是否正常：
![下载文件](https://cdn.ramostear.com/2019-03-08-18-26-00-2ded0b19d7834fbea458cb93a579d470.png "下载文件")


最后，我们测试一下文件上传功能是否正常。在进行测试之前，我们先看一下文件上传目录中存储的文件信息：

![文件上传目录](https://cdn.ramostear.com/2019-03-08-18-26-15-7cef263127954cc090d0ea07627b1c7e.png "文件上传目录")

接下来，我们选择一份需要上传的文件，然后点击**upload**按钮上传文件：

![上传文件](https://cdn.ramostear.com/2019-03-08-18-26-41-3ef0de1e16d14cda9b181511ae329c6f.png "上传文件")

此时，文件以及上传成功，我们再次观察文件上传目录中的文件信息，以验证文件是否成功写入磁盘：

![文件上传目录对比](https://cdn.ramostear.com/2019-03-08-18-26-55-7ae1ef770ad543a6b38c1ce8687e8638.png "文件上传目录对比")



# 6. 结束语

处理本章节的教程内容外，你还可以访问我的个人博客[RT社圈](https://www.ramostear.com)的[Spring Boot 2.0系列专栏文章](https://www.ramostear.com/archive/spring-boot.html)阅读更多的教程内容。


# 声明
本文作者ramostear, 原文标题: [Spring Boot（十一）— 文件上传和下载](https://www.ramostear.com/posts/2019-03-08/file-handling.html).原文链接: [https://www.ramostear.com/posts/2019-03-08/file-handling.html](https://www.ramostear.com/posts/2019-03-08/file-handling.html)
如需转载，请联系本文作者。
