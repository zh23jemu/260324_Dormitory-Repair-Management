package com.platform.warranty.controller;

import com.platform.warranty.domain.vo.RepairRecordCountOfType;
import com.platform.warranty.mapper.RepairRecordMapper;
import com.platform.warranty.service.IPostService;
import com.platform.warranty.service.IRepairRecordService;
import com.platform.warranty.service.IResourceService;
import com.platform.warranty.service.IUserService;
import com.platform.warranty.utils.AjaxResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/home")
public class HomeController {
    @Autowired
    private IUserService userService;
    @Autowired
    private IPostService postService;
    @Autowired
    private IRepairRecordService  repairRecordService;
    @Autowired
    private IResourceService resourceService;
    @Autowired
    private RepairRecordMapper repairRecordMapper;

    @RequestMapping("/getCountData")
    public AjaxResult<HashMap<String, Object>> getCountData() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("userCount", userService.count());
        data.put("resourceCount", resourceService.count());
        data.put("repairRecordCount", repairRecordService.count());
        data.put("postCount", postService.count());
        return AjaxResult.success(data);
    }
    @GetMapping("/getProportion")
    public AjaxResult<List<RepairRecordCountOfType>> getProportion() {
        return AjaxResult.success(repairRecordMapper.selectRepairRecordCountOfType());
    }
}
