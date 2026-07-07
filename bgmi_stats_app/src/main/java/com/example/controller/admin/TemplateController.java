package com.example.controller.admin;

import java.util.List;

import com.example.dao.TemplateDao;
import com.example.model.admin.TemplateModel;

public class TemplateController {

    public static boolean createNewTemplate(TemplateModel model) {
        return TemplateDao.saveTemplate(model);
    }

    public static List<TemplateModel> getActiveTemplates() {
        return TemplateDao.fetchAllTemplates();
    }

    public static boolean deleteTemplate(String templateId) {
        return TemplateDao.deleteTemplate(templateId);
    }
}