package jp.ac.ems.controller.student;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jp.ac.ems.form.student.SelfStudyForm;
import jp.ac.ems.service.student.StudentSelfStudyService;

/**
 * 学生用自習Contollerクラス（student self study Controller Class）.
 * @author tejc999999
 */
@Controller
@RequestMapping("/student/selfstudy")
public class StudentSelfStudyController {
	
	/**
	 * 自習サービス
	 */
	@Autowired
	StudentSelfStudyService studentSelfStudyService;
	
    /**
     * モデルにフォームをセットする(set form the model).
     * @return 自習Form(self study form)
     */
    @ModelAttribute
    SelfStudyForm setupForm() {
        return new SelfStudyForm();
    }
    
    
    /**
     * 自習問題選択(select self study question).
     * 
     * @param form 自習Form(self study form)
     * @param result エラーチェック結果(error validate result)
     * @param model モデル(model)
     * @return 自習問題選択用ページビュー(select self study question page view)
     */
    @GetMapping(path = "select")
    String select(@Validated SelfStudyForm form, BindingResult result,
            Model model) {
    	
    	// ドロップダウン項目設定
    	studentSelfStudyService.setSelectData(form, model);
        
    	// チェックボックス項目設定
    	studentSelfStudyService.setCheckItems(form, model);
    	
        // 入力値保持
        model.addAttribute("selfStudyForm", form);

        return "student/selfstudy/select";
    }
    
    /**
     * 中分類取得(get field middle list).
     * @param form 自習Form(self study form)
     * @param result エラーチェック結果(error validate result)
     * @param model モデル(model)
     * @return 自習問題選択用ページビュー(select self study question page view)
     */
    @PostMapping(path = "select", params = "selectFieldLargeBtn")
    public String addSelectFieldMiddle(@Validated SelfStudyForm form, BindingResult result,
            Model model) {
        
        return select(form, result, model);
    }
    
    /**
     * 小分類取得(get field small list).
     * @param form 自習Form(self study form)
     * @param result エラーチェック結果(error validate result)
     * @param model モデル(model)
     * @return 自習問題選択用ページビュー(select self study question page view)
     */
    @PostMapping(path = "select", params = "selectFieldMiddleBtn")
    public String addSelectFieldSmall(@Validated SelfStudyForm form, BindingResult result,
            Model model) {
        
        return select(form, result, model);
    }

    /**
     * 自習問題条件による問題取得.
     * 
     * @param form 自習Form(self study form)
     * @param result エラーチェック結果(error validate result)
     * @param model モデル(model)
     * @return 自習問題選択用ページビュー(select self study question page view)
     */
    @PostMapping(path = "select", params = "selectBtn")
    String selectCondition(@Validated SelfStudyForm form, BindingResult result,
            Model model) {

    	SelfStudyForm selfStudyForm = studentSelfStudyService.getQuestionList(form);
    	model.addAttribute("selfStudyForm", selfStudyForm);
    	
    	// ドロップダウン項目設定
    	studentSelfStudyService.setSelectData(form, model);
    	
    	// チェックボックス項目設定
    	studentSelfStudyService.setCheckItems(form, model);

        return "student/selfstudy/select";
    }

}
