package jp.ac.ems.controller.teacher;

import java.util.List;
import java.util.Map;

import jp.ac.ems.form.teacher.ClassForm;
import jp.ac.ems.service.teacher.TeacherClassService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * 先生用クラスContollerクラス（teacher class Controller Class）.
 * @author tejc999999
 */
@Controller
@RequestMapping("/teacher/class_")
public class TeacherClassController {

    /**
     * クラス用サービス(class service).
     */
    @Autowired
    TeacherClassService classService;
    
    /**
     * モデルにフォームをセット(set form the model).
     * @return クラスForm(class form)
     */
    @ModelAttribute
    ClassForm setupForm() {
        return new ClassForm();
    }

    /**
     * クラス一覧ページ表示(show class list page).
     * @param model クラス一覧保存用モデル(model to save class list)
     * @return クラス一覧ページビュー(class list page view)
     */
    @GetMapping
    String list(ClassForm form, Model model) {

        List<ClassForm> classFormList = classService.findAll();

        model.addAttribute("classes", classFormList);

        return "teacher/class_/list";
    }

    /**
     * クラス登録ページ表示(show add class page).
     * @return クラス登録ページビュー(add class page view)
     */
    @GetMapping(path = "add")
    public String add(Model model) {
        
        Map<String, String> userMap = classService.getUserIdMap();
        model.addAttribute("userCheckItems", userMap);

        return "teacher/class_/add";
    }

    /**
     * クラス登録処理(add process for class).
     * @return クラス一覧ページリダイレクト(redirect class list page)
     */
    @PostMapping(path = "add")
    public String addProcess(@Validated ClassForm form, BindingResult result,
            Model model) {

    	if(result.hasErrors()) {
    		
    		model.addAttribute("classForm", form);
            Map<String, String> userMap = classService.getUserIdMap();
            model.addAttribute("userCheckItems", userMap);
    		return "teacher/class_/add";
    	}
        classService.save(form);

        return "redirect:/teacher/class_";
    }

    /**
     * クラス編集ページ表示(show edit class page).
     * @return クラス編集ページビュー(edit class page view)
     */
    @PostMapping(path = "edit")
    public String edit(@RequestParam String id, Model model) {

        // チェックボックスのユーザー一覧を取得する
        Map<String, String> userMap = classService.getUserIdMap();
        model.addAttribute("userCheckItems", userMap);

        // クラス選択情報を設定する
        ClassForm classForm = classService.findById(id);
        model.addAttribute("classForm", classForm);

        return "teacher/class_/edit";
    }

    /**
     * クラス編集処理(edit process for class).
     * @return クラス一覧ページリダイレクト(class list page redirect)
     */
    @PostMapping(path = "editprocess")
    public String editProcess(@Validated ClassForm form, BindingResult result, Model model) {

    	if(result.hasErrors()) {
    		
            // チェックボックスのユーザー一覧を取得する
            Map<String, String> userMap = classService.getUserIdMap();
            model.addAttribute("userCheckItems", userMap);
            
            model.addAttribute("classForm", form);
            
            return "teacher/class_/edit";
    	}
        classService.save(form);

        return "redirect:/teacher/class_";
    }

    /**
     * クラス削除処理(delete process for class).
     * @return クラス一覧ページリダイレクト(redirect class list page)
     */
    @PostMapping(path = "delete")
    public String delete(@RequestParam String id, Model model) {

        classService.delete(id);

        return "redirect:/teacher/class_";
    }
}
