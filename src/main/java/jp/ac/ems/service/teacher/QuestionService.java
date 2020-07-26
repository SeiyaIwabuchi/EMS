package jp.ac.ems.service.teacher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jp.ac.ems.bean.QuestionBean;
import jp.ac.ems.config.FieldLarge;
import jp.ac.ems.config.FieldMiddle;
import jp.ac.ems.config.FieldSmall;
import jp.ac.ems.form.teacher.QuestionForm;
import jp.ac.ems.repository.QuestionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;


/**
 * 先生用問題Serviceクラス（teacher question Service Class）.
 * @author tejc999999
 *
 */
@Service
public class QuestionService {

    /**
     * 問題リポジトリ(question repository).
     */
    @Autowired
    QuestionRepository questionRepository;
    
    /**
     * 全ての問題を取得する(get all question).
     * @return 全ての問題Formリスト
     */
    public List<QuestionForm> findAll() {
        List<QuestionForm> list = new ArrayList<QuestionForm>();

        for (QuestionBean questionBean : questionRepository.findAll()) {
            QuestionForm questionForm = new QuestionForm();
            questionForm.setId(String.valueOf(questionBean.getId()));
//            questionForm.setTitle(questionBean.getTitle());
//            questionForm.setDescription(questionBean.getDescription());
//            questionForm.setInputNum(questionBean.getInputNum());
            list.add(questionForm);
        }
        
        return list;
    }
        
    /**
     * 問題を取得する.
     * @param id 問題ID
     * @return 問題Form
     */
    public QuestionForm findById(String id) {

        QuestionForm form = new QuestionForm();
        Optional<QuestionBean> optQuestion = questionRepository.findById(Long
                .parseLong(id));
        optQuestion.ifPresent(questionBean -> {
            form.setId(String.valueOf(questionBean.getId()));
//            form.setTitle(questionBean.getTitle());
//            form.setDescription(questionBean.getDescription());
//            form.setInputNum(questionBean.getInputNum());
        });
        
        return form;
    }
    

    
    /**
     * 問題を登録する.
     * @param form 問題Form
     * @return 登録済み問題Form
     */
    public QuestionForm save(QuestionForm form) {

        QuestionBean questionBean = new QuestionBean();
        String questionId = form.getId();
        if (questionId != null && !questionId.equals("")) {
            questionBean.setId(Long.parseLong(questionId));
        }
//        questionBean.setTitle(form.getTitle());
//        questionBean.setDescription(form.getDescription());
//        questionBean.setInputNum((byte) form.getInputNum());
        
        questionBean = questionRepository.save(questionBean);
        
        QuestionForm resultQuestionForm = new QuestionForm();
        resultQuestionForm.setId(String.valueOf(questionId));
//        resultQuestionForm.setTitle(questionBean.getTitle());
//        resultQuestionForm.setDescription(questionBean.getDescription());
//        resultQuestionForm.setInputNum(questionBean.getInputNum());
        
        return resultQuestionForm;
    }
    
    /**
     * 問題削除.
     * @param id 問題ID
     */
    public void delete(String id) {

        QuestionBean questionBean = new QuestionBean();
        questionBean.setId(Long.parseLong(id));

        questionRepository.delete(questionBean);
    }
}
