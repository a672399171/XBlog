package com.zzu.xblog.service;

import com.zzu.xblog.common.Common;
import com.zzu.xblog.dto.Result;
import com.zzu.xblog.model.UploadType;
import com.zzu.xblog.util.Utils;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件相关service
 */
@Service
public class FileService {
    private final Logger logger = LogManager.getLogger(getClass());

    /**
     * 上传头像图片
     *
     * @param file
     * @param request
     * @return
     */
    public Map<String, Object> uploadPhoto(MultipartFile file, HttpServletRequest request) {

        Map<String, Object> result = new HashMap<String, Object>();
        result.put(Common.SUCCESS, false);
        if (file.getSize() > 1024 * 1024) {
            result.put(Common.MSG, "文件过大：不超过1MB");
            return result;
        }
        String fileName = file.getOriginalFilename();
        String fileFormat = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (!Arrays.asList(UploadType.IMAGE.getFormats()).contains(fileFormat)) {
            result.put(Common.MSG, "文件格式错误");
            return result;
        }

        return uploadFileCore(file, request, "images");
    }

    /**
     * 上传文件
     *
     * @param file
     * @param uploadType
     * @param request
     * @return
     */
    public Map<String, Object> uploadFiles(MultipartFile file, UploadType uploadType, HttpServletRequest request) {
        String fileName = file.getOriginalFilename();
        String fileFormat = fileName.substring(fileName.lastIndexOf(".") + 1);
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("error", 1);

        if (file.getSize() > uploadType.getMaxSize()) {
            result.put("message", "文件过大：不超过" + uploadType.getMaxSize() / 1000 + "KB");
            return result;
        } else if (!Arrays.asList(uploadType.getFormats()).contains(fileFormat)) {
            result.put("message", "文件格式错误，支持的格式：" + Arrays.toString(uploadType.getFormats()));
        } else {
            result = uploadFileCore(file, request, uploadType.getFolder());
            if ((Boolean) result.get(Common.SUCCESS)) {
                result.put("error", 0);
                result.put("url", request.getContextPath() + "/" + result.get(Common.FILENAME));
            } else {
                result.put("message", result.get(Common.MSG));
            }
        }

        return result;
    }

    /**
     * 裁剪并缩放图片
     *
     * @param fileName
     * @param x
     * @param y
     * @param width
     * @param height
     * @return
     */
    public Result cropper(String fileName, double x, double y, double width, double height, HttpServletRequest request) {
        Result result = new Result();
        result.setSuccess(true);

        fileName = request.getSession().getServletContext().getRealPath("/") + fileName;
        try {
            Thumbnails.of(new File(fileName))
                    .sourceRegion((int) x, (int) y, (int) width, (int) height)
                    .size(Common.PHOTO_SIZE, Common.PHOTO_SIZE)
                    .toFile(new File(fileName));
        } catch (IOException e) {
            e.printStackTrace();
            result.setSuccess(false);
            result.setMsg("内部错误");
        }

        return result;
    }

    /**
     * 上传文件核心方法
     *
     * @param file
     * @param request
     * @param childPath
     * @return
     */
    private Map<String, Object> uploadFileCore(MultipartFile file, HttpServletRequest request, String childPath) {
        Map<String, Object> result = new HashMap<String, Object>();
        String path = request.getSession().getServletContext().getRealPath("/");
        File folder = new File(path + childPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String fileName = file.getOriginalFilename();
        String newFileName = childPath + "/" + Utils.uuid() + fileName.substring(fileName.lastIndexOf("."));
        String newFilePath = path + newFileName;
        try {
            file.transferTo(new File(newFilePath));
        } catch (IOException e) {
            result.put(Common.SUCCESS, false);
            result.put(Common.MSG, "上传失败，内部错误!");
            e.printStackTrace();
            return result;
        }
        result.put(Common.SUCCESS, true);
        result.put(Common.FILENAME, newFileName);

        logger.info(newFilePath);
        return result;
    }

}
