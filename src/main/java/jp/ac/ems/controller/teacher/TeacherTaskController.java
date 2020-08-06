package jp.ac.ems.controller.teacher;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import jp.ac.ems.form.teacher.TaskForm;
import jp.ac.ems.form.teacher.TaskSubmissionForm;
import jp.ac.ems.impl.service.teacher.TeacherTaskServiceImpl;
import jp.ac.ems.service.teacher.TeacherTaskService;

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
 * 先生用課題Contollerクラス（teacher task Controller Class）.
 * @author tejc999999
 */
@Controller
@RequestMapping("/teacher/task")
public class TeacherTaskController {

    @Autowired
    TeacherTaskService taskService;

//    @Autowired
//    HttpSession session;
    
//    @Autowired
//    QuestionService questionService;

    /**
     * モデルにフォームをセットする(set form the model).
     * @return 課題Form(task form)
     */
    @ModelAttribute
    TaskForm setupForm() {
        return new TaskForm();
    }
    
    /**
     * 課題一覧(question list).
     * @param model 問題一覧保存用モデル(model to save question list)
     * @return 課題登録用問題一覧ページビュー(question list page view for task add)
     */
    @GetMapping
    String list(Model model) {

        List<TaskForm> list = taskService.findAll();

        model.addAttribute("tasks", list);

        return "teacher/task/list";
    }

    /**
     * 課題編集(task edit).
     * @return 課題情報登録用ページビュー(task info add page view)
     */
    @PostMapping(path = "edit")
    public String edit(@RequestParam String id, Model model) {

        TaskForm taskForm = taskService.findById(id);

        model.addAttribute("taskForm", taskForm);
    	
        return "/teacher/task/edit";
    }
    
    /**
     * 課題編集処理(edit process for task).
     * @return 課題一覧ページリダイレクト(task list page redirect)
     */
    @PostMapping(path = "editprocess")
    public String editprocess(TaskForm form, Model model) {

    	taskService.save(form);
    	
        return "redirect:/teacher/task";
    }
    

    /**
     * 課題削除(task delete).
     * @return 課題一覧ページリダイレクト(task list page redirect)
     */
    @PostMapping(path = "delete")
    public String delete(@RequestParam String id, Model model) {

    	// 削除処理
    	taskService.delete(id);
    	
        return "redirect:/teacher/task";
    }
    
    /**
     * 課題登録(task add).
     * @return 課題情報登録用ページビュー(task info add page view)
     */
    @GetMapping(path = "add")
    public String add() {

//    	session.removeAttribute("taskForm");
    	
        return "/teacher/task/add";
    }

    
    /**
     * 課題登録 - 問題追加(task add/question add).
     * @param form 課題Form(task form)
     * @param result エラーチェック結果(error validate result)
     * @param model モデル(model to save xxx)
     * @return 課題問題登録用ページビュー(task question add page view)
     */
    @PostMapping(path = "add_question")
    public String addQuestion(@Validated TaskForm form, BindingResult result,
            Model model) {

    	// 年度取得
        Map<String, String> yearMap = taskService.findAllYearMap();
        model.addAttribute("yearDropItems", yearMap);
    	
    	// 大分類取得
        Map<String, String> fieldLMap = taskService.findAllFieldLMap();
        model.addAttribute("fieldLDropItemsItems", fieldLMap);
    	
    	// 中分類取得
        Map<String, String> fieldMMap = taskService.findAllFieldMMap(form.getSelectFieldL());
        model.addAttribute("fieldMDropItems", fieldMMap);
    	
    	// 小分類取得
        Map<String, String> fieldSMap = taskService.findAllFieldSMap(form.getSelectFieldM());
        model.addAttribute("fieldSDropItems", fieldSMap);
    	
        // 全問題取得
        Map<String, String> questionMap = taskService.findAllMap();
        model.addAttribute("questionCheckItems", questionMap);
        
//    	session.setAttribute("taskForm", form);
        
        return "/teacher/task/add_question";
    }

    /**
     * 課題登録 - 送信先追加(task add/send to add).
     * @param form 課題Form(task form)
     * @param result エラーチェック結果(error validate result)
     * @param model モデル(model to save xxx)
     * @return 課題送信先登録用ページビュー(task send to  add page view)
     */
    @PostMapping(path = "add_submit")
    public String addSubmit(@Validated TaskForm form, BindingResult result,
            Model model) {

    	// TODO:作成中
    	// コース一覧
        Map<String, String> courseMap = taskService.findAllCourse();
        model.addAttribute("courseCheckItems", courseMap);
    	
        // 全クラス取得
        Map<String, String> classMap = taskService.findAllClass();
        model.addAttribute("classCheckItems", classMap);

        // 全学生取得
        Map<String, String> userMap = taskService.findAllStudent();
        model.addAttribute("userCheckItems", userMap);
        
//    	session.setAttribute("taskForm", form);
        
        return "/teacher/task/add_submit";
    }
    

    /**
     * 課題問題登録画面から課題情報登録画面に戻る(Return to the task question add view from the task info add view)
     * @param form 課題Form(task form)
     * @param result エラーチェック結果(error validate result)
     * @param model モデル(model to save xxx)
     * @return 課題問題登録用ページビュー(task question add page view)
     */
    @PostMapping(path = "add_submit", params = "backBtn")
    public String addSubmitBack(@Validated TaskForm form, BindingResult result,
            Model model) {
        
//    	model.addAttribute("taskForm", form);
    	
        return "/teacher/task/add";
    }
    
    /**
     * 課題送信先画面から課題問題登録画面に戻る(Return to the task add view from the task destination view).
     * @param form 課題Form(task form)
     * @param result エラーチェック結果(error validate result)
     * @param model モデル(model to save xxx)
     * @return 課題問題登録用ページビュー(task question add page view)
     */
    @PostMapping(path = "add_process", params = "backBtn")
    public String addProcessBack(@Validated TaskForm form, BindingResult result,
            Model model) {
        
//    	model.addAttribute("taskForm", form);
    	
        return addQuestion(form, result, model);
    }

    /**
     * 所属要素除外(exclution).
     * @param form 課題Form(task form)
     * @param result エラーチェック結果(error validate result)
     * @param model モデル(model to save xxx)
     * @return 課題問題登録用ページビュー(task question add page view)
     */
    @PostMapping(path = "add_process", params = "exclusionBtn")
    public String addProcessExclution(@Validated TaskForm form, BindingResult result,
            Model model) {
    	
    	// 全コースを取得する
        Map<String, String> courseMap = taskService.findAllCourse();
        model.addAttribute("courseCheckItems", courseMap);
        
    	// コースに所属するクラスを除外した全クラスを取得する
        Map<String, String> classMap = taskService.findAllClass(form.getCourseCheckedList());
        model.addAttribute("classCheckItems", classMap);

        // コース（所属クラス）とクラスを除外した全ユーザを取得する
        Map<String, String> userMap = taskService.findAllStudent(form.getCourseCheckedList(), form.getClassCheckedList());
        model.addAttribute("userCheckItems", userMap);

//    	model.addAttribute("taskForm", form);
    	
        return "/teacher/task/add_submit";
    }

    
    
    /**
     * 課題登録処理(add process for task).
     * @param form 課題Form(task form)
     * @param result エラーチェック結果(error validate result)
     * @param model モデル(model to save xxx)
     * @return 課題一覧画面リダイレクト(task list view redirect)
     */
    @PostMapping(path = "add_process")
    public String addProcess(@Validated TaskForm form, BindingResult result,
            Model model) {

        // コース情報をDBに保存する
        taskService.save(form);
        
        return "redirect:/teacher/task";
    }

    /**
     * 年度別問題取得(Obtaining questions by year).
     * @param form 課題Form(task form)
     * @param result エラーチェック結果(error validate result)
     * @param model モデル(model)
     * @return 課題問題登録用ページビュー(task question add page view)
     */
    @PostMapping(path = "add_submit", params = "selectYearBtn")
    public String addSelectYear(@Validated TaskForm form, BindingResult result,
            Model model) {

    	Map<String, String> questionMap = taskService.findAllQuestionByYearAndTerm(form.getSelectYear());
    	model.addAttribute("questionCheckItems", questionMap);
    	
    	// 年度取得
        Map<String, String> yearMap = taskService.findAllYearMap();
        model.addAttribute("yearDropItems", yearMap);
    	
    	// 大分類取得
        Map<String, String> fieldLMap = taskService.findAllFieldLMap();
        model.addAttribute("fieldLDropItemsItems", fieldLMap);
    	
    	// 中分類取得
        Map<String, String> fieldMMap = taskService.findAllFieldMMap(form.getSelectFieldL());
        model.addAttribute("fieldMDropItems", fieldMMap);
    	
    	// 小分類取得
        Map<String, String> fieldSMap = taskService.findAllFieldSMap(form.getSelectFieldM());
        model.addAttribute("fieldSDropItems", fieldSMap);
    	
        // 入力状態保持のため
        model.addAttribute("courseForm", form);
        
        return "/teacher/task/add_question";
    }
    
    /**
     * 中分類取得(get field middle list).
     * @param form 課題Form(task form)
     * @param result エラーチェック結果(error validate result)
     * @param model モデル(model)
     * @return 課題問題登録用ページビュー(task question add page view)
     */
    @PostMapping(path = "add_submit", params = "selectFieldLargeBtn")
    public String addSelectFieldMiddle(@Validated TaskForm form, BindingResult result,
            Model model) {
        
        return addQuestion(form, result, model);
    }
    
    /**
     * 小分類取得(get field small list).
     * @param form 課題Form(task form)
     * @param result エラーチェック結果(error validate result)
     * @param model モデル(model)
     * @return 課題問題登録用ページビュー(task question add page view)
     */
    @PostMapping(path = "add_submit", params = "selectFieldMiddleBtn")
    public String addSelectFieldSmall(@Validated TaskForm form, BindingResult result,
            Model model) {
        
        return addQuestion(form, result, model);
    }
    
    /**
     * 分野別問題取得(Obtaining questions by field).
     * @param form 課題Form(task form)
     * @param result エラーチェック結果(error validate result)
     * @param model モデル(model)
     * @return 課題問題登録用ページビュー(task question add page view)
     */
    @PostMapping(path = "add_submit", params = "selectFieldBtn")
    public String addSelectField(@Validated TaskForm form, BindingResult result,
            Model model) {

    	Map<String, String> questionMap = taskService.findAllQuestionByField(form.getSelectFieldL(), form.getSelectFieldM(), form.getSelectFieldS());
    	model.addAttribute("questionCheckItems", questionMap);
    	
    	// 年度取得
        Map<String, String> yearMap = taskService.findAllYearMap();
        model.addAttribute("yearDropItems", yearMap);
    	
    	// 大分類取得
        Map<String, String> fieldLMap = taskService.findAllFieldLMap();
        model.addAttribute("fieldLDropItemsItems", fieldLMap);
    	
    	// 中分類取得
        Map<String, String> fieldMMap = taskService.findAllFieldMMap(form.getSelectFieldL());
        model.addAttribute("fieldMDropItems", fieldMMap);
    	
    	// 小分類取得
        Map<String, String> fieldSMap = taskService.findAllFieldSMap(form.getSelectFieldM());
        model.addAttribute("fieldSDropItems", fieldSMap);
    	
        // 入力状態保持のため
        model.addAttribute("courseForm", form);
        
        return "/teacher/task/add_question";
    }
    
    
    /**
     * 分野別問題取得(Obtaining questions by field).
     * @param form 課題Form(task form)
     * @param result エラーチェック結果(error validate result)
     * @param model モデル(model)
     * @return 課題問題登録用ページビュー(task question add page view)
     */
    @PostMapping(path = "submissionlist")
    public String submissionList(@RequestParam String id, Model model) {

    	
    	model.addAttribute("submissionTasks", taskService.getAnswerdList(id));
    	

        return "/teacher/task/submissionlist";
    }
    
}
