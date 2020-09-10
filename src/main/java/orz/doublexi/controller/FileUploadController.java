package orz.doublexi.controller;

import org.apache.commons.lang.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.view.RedirectView;
import orz.doublexi.pojo.Taken;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class FileUploadController {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadController.class);
    private HashMap<String, Taken> takens = new HashMap<>();
    @RequestMapping("/getlist")
    @ResponseBody
    public Map listUploadedFiles(@RequestParam(value = "taken",required = false) String taken) throws IOException {
        LOGGER.info("path="+new File("").getAbsolutePath());
        LOGGER.info("taken="+taken);
        if (takens.get(taken)!=null&&takens.get(taken).getRestTime() > 0) {
        return getPersonalFileList(takens.get(taken));
        }
        return getDefFileList();
    }

    @RequestMapping("/file/{filename:.+}/file/{taken}")
    @ResponseBody
    public ResponseEntity serveFile(@PathVariable String filename,@PathVariable String taken) {
            Resource file = new FileSystemResource(takens.get(taken).getPath()+File.separator+filename);
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + file.getFilename() + "\"").body(file);

    }
    @RequestMapping("/file/{filename:.+}/directory/{taken}")
    public RedirectView serveDirectory(@PathVariable String filename, @PathVariable String taken) {
         LOGGER.info("serveDirectory method executed");
        Taken tak = takens.get(taken);
        tak.setPath(tak.getPath()+File.separator+filename);
        tak.setMessage("进入到目录:"+tak.getPath());
        return new RedirectView("/index.html");
    }
    @RequestMapping("/back/{taken}")
    public RedirectView back(@PathVariable String taken) {
         LOGGER.info("back method executed");
        Taken tak = takens.get(taken);
        File parentFile = new File(tak.getPath()).getParentFile();
        tak.setPath(parentFile.getAbsolutePath());

        tak.setMessage("后退到目录:"+tak.getPath());
        return new RedirectView("/index.html");
    }

    @RequestMapping("/upload/{taken}")
    @ResponseBody
    public Map<String, String> handleFileUpload(@RequestParam("file") MultipartFile file,@PathVariable String taken) {
         LOGGER.info("handleFileUpload method executed");
        Taken tak = takens.get(taken);
        String fileName = UUID.randomUUID().toString().substring(0,5)+"_"+file.getOriginalFilename();
        HashMap<String, String> response = new HashMap<>();
        try {
            file.transferTo(new File(new File(tak.getPath()+File.separator+fileName).getAbsolutePath()));
        } catch (Exception e) {
            tak.setMessage("出现错误"+e.getMessage());
            response.put("status", "no");
            response.put("msg", "失败了");
            return response;
        }

        response.put("status", "ok");
        response.put("msg", "OK上传成功");
        tak.setMessage("上传成功,文件更名为:"+fileName);

        return response;
    }
    private Map getDefFileList(){
        LOGGER.info("getDefFileList method executed");
        File file = new File(new File("").getAbsolutePath());
        File[] files = file.listFiles();
        List<File> fileList = Arrays.asList(files);
        List<HashMap> collect = fileList.stream().sorted(Comparator.comparing(File::lastModified)).map(f -> {
            HashMap map = new HashMap();
            map.put("name", f.getName());
            map.put("type", f.isDirectory() ? "directory" : "file");
            map.put("size", String .format("%.2f",f.length()/1024d)+" KB");
            map.put("modifytime", DateFormatUtils.ISO_DATE_FORMAT.format(new Date(f.lastModified())));
            return map;
        }).collect(Collectors.toList());
        HashMap map = new HashMap();
        String taken = UUID.randomUUID().toString();
        Taken tak = new Taken(taken);
        tak.setPath(new File("").getAbsolutePath());
        takens.put(tak.getKey(), tak);
        map.put("status", "NO");
        map.put("fileList", collect);
        map.put("taken", taken);
        tak.setMessage("当前目录:"+tak.getPath());
        map.put("message",tak.getMessage());
        return map;
    }

    private Map getPersonalFileList(Taken taken){
        LOGGER.info("getPersonalFileList method executed");
        File file = new File(taken.getPath());
        LOGGER.info(file.getAbsolutePath());
        File[] files = file.listFiles();
        List<File> fileList = Arrays.asList(files);
        List<HashMap> collect = fileList.stream().sorted(Comparator.comparing(File::lastModified)).map(f -> {
            HashMap map = new HashMap();
            map.put("name", f.getName());
            map.put("type", f.isDirectory() ? "directory" : "file");
            map.put("size", String .format("%.2f",f.length()/1024d)+" KB");
            map.put("modifytime", DateFormatUtils.ISO_DATE_FORMAT.format(new Date(f.lastModified())));

            return map;
        }).collect(Collectors.toList());
        HashMap map = new HashMap();
        map.put("status", "OK");
        map.put("fileList", collect);
        map.put("message",taken.getMessage());
        return map;
    }


}