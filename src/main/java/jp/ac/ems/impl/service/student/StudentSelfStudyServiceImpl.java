package jp.ac.ems.impl.service.student;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import jp.ac.ems.bean.ClassBean;
import jp.ac.ems.bean.CourseBean;
import jp.ac.ems.bean.QuestionBean;
import jp.ac.ems.bean.StudentQuestionHistoryBean;
import jp.ac.ems.bean.StudentTaskBean;
import jp.ac.ems.bean.TaskBean;
import jp.ac.ems.bean.TaskQuestionBean;
import jp.ac.ems.bean.UserBean;
import jp.ac.ems.config.ExamDivisionCode;
import jp.ac.ems.config.FieldLarge;
import jp.ac.ems.config.FieldMiddle;
import jp.ac.ems.config.FieldSmall;
import jp.ac.ems.config.QuestionTag;
import jp.ac.ems.form.student.SelfStudyForm;
import jp.ac.ems.form.student.SelfStudyQuestionForm;
import jp.ac.ems.form.teacher.TaskForm;
import jp.ac.ems.repository.QuestionRepository;
import jp.ac.ems.repository.QuestionTagRepository;
import jp.ac.ems.repository.StudentQuestionHistoryRepository;
import jp.ac.ems.repository.TaskRepository;
import jp.ac.ems.repository.UserRepository;
import jp.ac.ems.service.student.StudentSelfStudyService;
import jp.ac.ems.service.util.JPCalenderEncoder;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 学生用自習サービスクラス(self study service class for student).
 * 
 * @author user01-m
 */
@Service
public class StudentSelfStudyServiceImpl implements StudentSelfStudyService {

	/**
	 * ユーザーリポジトリ(user repository)
	 */
	@Autowired
	private UserRepository userRepository;
	
	/**
	 * 問題リポジトリ(quesiton repository)
	 */
	@Autowired
	private QuestionRepository questionRepository;
	
	/**
	 * 学生問題回答履歴リポジトリ(student question history repository)
	 */
	@Autowired
	private StudentQuestionHistoryRepository studentQuestionHistoryRepository;

	/**
	 * 課題リポジトリ(task repository)
	 */
	@Autowired
	private TaskRepository taskRepository;
	
    /**
     * ドロップダウン項目設定(Set dropdown param).
     * @param form 自習Form(self study form)
     * @param model モデル(model)
     */
	@Override
    public void setSelectData(SelfStudyForm form, Model model) {
    	// 年度取得
        Map<String, String> yearMap = findAllYearMap();
        model.addAttribute("yearDropItems", yearMap);
    	
    	// 大分類取得
        Map<String, String> fieldLMap = findAllFieldLMap();
        model.addAttribute("fieldLDropItemsItems", fieldLMap);
    	
    	// 中分類取得
        Map<String, String> fieldMMap = findAllFieldMMap(form.getSelectFieldL());
        model.addAttribute("fieldMDropItems", fieldMMap);
    	
    	// 小分類取得
        Map<String, String> fieldSMap = findAllFieldSMap(form.getSelectFieldM());
        model.addAttribute("fieldSDropItems", fieldSMap);
    	
    }

	/**
     * セレクトボックス項目設定(Set select box param).
     * @param form 自習Form(self study form)
     * @param model モデル(model)
	 */
	@Override
	public void setCheckItems(SelfStudyForm form, Model model) {
		Map<String, String> conditionCheckMap = new LinkedHashMap<>();
		conditionCheckMap.put(SelfStudyForm.CONDITION_1_KEY_UNANSWERED, SelfStudyForm.CONDITION_1_VALUE_UNANSWERED);
		conditionCheckMap.put(SelfStudyForm.CONDITION_2_KEY_LOW_ACC_RATE, SelfStudyForm.CONDITION_2_VALUE_LOW_ACC_RATE);
		conditionCheckMap.put(SelfStudyForm.CONDITION_3_KEY_MIX, SelfStudyForm.CONDITION_3_VALUE_MIX);
		conditionCheckMap.put(SelfStudyForm.CONDITION_4_KEY_ALL, SelfStudyForm.CONDITION_4_VALUE_ALL);
		model.addAttribute("conditionCheckItems", conditionCheckMap);
		
		Map<String, String> sortCheckMap = new LinkedHashMap<>();
		sortCheckMap.put(SelfStudyForm.SORT_1_KEY_LATEST, SelfStudyForm.SORT_1_VALUE_LATEST);
		sortCheckMap.put(SelfStudyForm.SORT_2_KEY_PREVIOUS, SelfStudyForm.SORT_2_VALUE_PREVIOUS);
		sortCheckMap.put(SelfStudyForm.SORT_3_KEY_RANDOM, SelfStudyForm.SORT_3_VALUE_RANDOM);
		model.addAttribute("sortCheckItems", sortCheckMap);
	}
	
	/**
	 * 条件に該当する問題IDリストを取得する.
	 * 
	 * @param form 自習Form(self study form)
	 * @return 自習Form(self study form)
	 */
	@Override
	public SelfStudyForm getQuestionList(SelfStudyForm form) {
		
		List<QuestionBean> questionBeanList = new ArrayList<>();
		
		// 年度：分野の条件なし
		if((form.getSelectYear() == null || form.getSelectYear().equals(""))
				&& (form.getSelectFieldS() == null || form.getSelectFieldS().equals(""))
						&& (form.getSelectFieldM() == null || form.getSelectFieldM().equals(""))
								&& (form.getSelectFieldL() == null || form.getSelectFieldL().equals(""))) {
			List<QuestionBean> list = questionRepository.findAll();
			if(list != null) {
				questionBeanList.addAll(list);
			}
		} else {
			if((form.getSelectYear() != null && !form.getSelectYear().equals(""))) {
				// 年度条件あり
				String yearStr = form.getSelectYear().substring(0, 4);
				String termStr = form.getSelectYear().substring(4, 5);

				if(form.getSelectFieldS() != null && !form.getSelectFieldS().equals("")) {
					// 年度＋小分野条件
					List<QuestionBean> list = questionRepository.findByYearAndTermAndFieldSId(yearStr, termStr, Byte.valueOf(form.getSelectFieldS()));
					if(list != null) {
						questionBeanList.addAll(list);
					}
				} else if(form.getSelectFieldM() != null && !form.getSelectFieldM().equals("")) {
					// 年度＋中分野条件
					List<QuestionBean> list = questionRepository.findByYearAndTermAndFieldMId(yearStr, termStr, Byte.valueOf(form.getSelectFieldM()));
					if(list != null) {
						questionBeanList.addAll(list);
					}
				} else if((form.getSelectFieldL() != null && !form.getSelectFieldL().equals(""))) {
					// 年度＋大分野条件
					List<QuestionBean> list = questionRepository.findByYearAndTermAndFieldLId(yearStr, termStr, Byte.valueOf(form.getSelectFieldL()));
					if(list != null) {
						questionBeanList.addAll(list);
					}
				} else {
					// 年度条件のみ
					List<QuestionBean> list = questionRepository.findByYearAndTerm(yearStr, termStr);
					if(list != null) {
						questionBeanList.addAll(list);
					}
				}
			} else {
				// 年度条件なし
				if(form.getSelectFieldS() != null && !form.getSelectFieldS().equals("")) {
					// 小分野条件
					List<QuestionBean> list = questionRepository.findByFieldSId(Byte.valueOf(form.getSelectFieldS()));
					if(list != null) {
						questionBeanList.addAll(list);
					}
				} else if(form.getSelectFieldM() != null && !form.getSelectFieldM().equals("")) {
					// 中分野条件
					List<QuestionBean> list = questionRepository.findByFieldMId(Byte.valueOf(form.getSelectFieldM()));
					if(list != null) {
						questionBeanList.addAll(list);
					}
				} else if((form.getSelectFieldL() != null && !form.getSelectFieldL().equals(""))) {
					// 大分野条件
					List<QuestionBean> list = questionRepository.findByFieldLId(Byte.valueOf(form.getSelectFieldL()));
					if(list != null) {
						questionBeanList.addAll(list);
					}
				}
			}
		}
		
		List<String> questionIdList = new ArrayList<>();
		for(QuestionBean bean : questionBeanList) {
			questionIdList.add(String.valueOf(bean.getId()));
		}

		// 条件による除外
		Map<String, RateData> questionIdForIncorrect50 = new LinkedHashMap<>();
		List<String> tempRemoveQuetionId = new ArrayList<>();
		if(form.getConditionChecked() != null && !form.getConditionChecked().equals(SelfStudyForm.CONDITION_4_KEY_ALL)) {

	        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	        String userId = auth.getName();
	        List<StudentQuestionHistoryBean> sqhBeanList = studentQuestionHistoryRepository.findAllByUserId(userId);
	        for(StudentQuestionHistoryBean bean : sqhBeanList) {
	
				if(form.getConditionChecked().equals(SelfStudyForm.CONDITION_1_KEY_UNANSWERED)
						|| form.getConditionChecked().equals(SelfStudyForm.CONDITION_3_KEY_MIX)) {
		        	// 回答済み除外（未回答のみ）
					
					tempRemoveQuetionId.add(String.valueOf(bean.getQuestionId()));
				}
				if(form.getConditionChecked().equals(SelfStudyForm.CONDITION_2_KEY_LOW_ACC_RATE)
						|| form.getConditionChecked().equals(SelfStudyForm.CONDITION_3_KEY_MIX)) {
					// 低回答率（50%以下）のみを別リストに退避する（正解数ゼロ、不正解数１以上の正解率0%を含む）
					
			        	if(questionIdList.contains(String.valueOf(bean.getQuestionId()))) {
			        		
			        		RateData rateData = null;
			        		if(questionIdForIncorrect50.containsKey(String.valueOf(bean.getQuestionId()))) {
			        			rateData = questionIdForIncorrect50.get(String.valueOf(bean.getQuestionId()));
			        		} else {
			        			rateData = new RateData();
			        		}
			        		if(bean.getCorrectFlg()) {
			        			rateData.setCorrectCnt(rateData.getCorrectCnt() + 1);
			        		} else {
			        			rateData.setIncorrectCnt(rateData.getIncorrectCnt() + 1);
			        		}
			        		questionIdForIncorrect50.put(String.valueOf(bean.getQuestionId()), rateData);
			        	}
				}
			}
	        // 低回答率が含まれるか検証するため、検証後にリストから除外する
	        questionIdList.removeAll(tempRemoveQuetionId);
	        
	        if(form.getConditionChecked().equals(SelfStudyForm.CONDITION_2_KEY_LOW_ACC_RATE)) {
	        	// 一旦リストを空にする
	        	questionIdList = new ArrayList<>();
	        	if(questionIdForIncorrect50 != null) {
	        		for(Map.Entry<String, RateData> entry : questionIdForIncorrect50.entrySet()) {
	        			if(entry.getValue().getCorrectCnt() <= entry.getValue().getIncorrectCnt())
	        				questionIdList.add(entry.getKey());
	        		}
	        	}
	        } else if(form.getConditionChecked().equals(SelfStudyForm.CONDITION_3_KEY_MIX)) {
	        	if(questionIdForIncorrect50 != null) {
	        		for(Map.Entry<String, RateData> entry : questionIdForIncorrect50.entrySet()) {
	        			if(entry.getValue().getCorrectCnt() <= entry.getValue().getIncorrectCnt())
	        				questionIdList.add(entry.getKey());
	        		}
	        	}
	        }
		}
		
		if(form.getQuestionTag() != null && form.getQuestionTag().size() > 0) {
			// タグによる選択

        	List<String> tagQuestionIdList = new ArrayList<>();
	        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	        String userId = auth.getName();

	        List<UserBean> userBeanList = new ArrayList<>();
	        Optional<UserBean> optUser = userRepository.findById(userId);
	        optUser.ifPresent(userBean -> {
	        	userBeanList.add(userBean);
	        });
	        if(userBeanList.size() > 0) {
	        	UserBean userBean = userBeanList.get(0);
	        	List<String> tagIdList = form.getQuestionTag();
	        	for(String tagId : tagIdList) {
        			List<String> list = userBean.getQuestionIdListByTagId(Long.valueOf(tagId));
        			if(list != null && list.size() > 0) {
        				tagQuestionIdList.addAll(list);
        			}
	        	}
	        }
	        
	        // タグによる選択と一致し、かつそれ以前の条件に合致する問題のみ残す
	        List<String> tempQuestionIdList = new ArrayList<>();
        	for(String questionId : tagQuestionIdList) {
        		if(questionIdList.contains(questionId)) {
        			tempQuestionIdList.add(questionId);
        		}
        	}
			questionIdList = tempQuestionIdList;
		}
		
		if(form.isLatestFlg()) {
			// 直近6回分だけにする
			List<YearAndTermData> latestYearAndTermList = new ArrayList<>();
			
    		// 直近6回に該当する年度、期を取得する
	    	for(QuestionBean questionBean : questionRepository.findDistinctYearAndTerm()) {
	    		// 全年度、期を取得
	    		latestYearAndTermList.add(new YearAndTermData(Integer.valueOf(questionBean.getYear()), questionBean.getTerm()));
	    	}
	    	// 年度の降順、期の昇順でソート
	    	latestYearAndTermList = latestYearAndTermList.stream()
	    			.sorted(Comparator.comparing(YearAndTermData::getYear, Comparator.reverseOrder())
	    					.thenComparing(YearAndTermData::getTerm))
	    			.collect(Collectors.toList());
	    	// 先頭から6個だけ取得
	    	if(latestYearAndTermList.size() > 6) {
	    		latestYearAndTermList = latestYearAndTermList.subList(0, 6);
	    	}
	    	
			List<String> removeQuestionIdList = new ArrayList<>();
			for(String questionId : questionIdList) {
				// 直近6回より前の問題を除外する
				
				// TODO:問い合わせ回数軽減策検討
				int latestLastYear = latestYearAndTermList.get(latestYearAndTermList.size() - 1).getYear();
				String latestLastTerm = latestYearAndTermList.get(latestYearAndTermList.size() - 1).getTerm();
				Optional<QuestionBean> optQuestion = questionRepository.findById(Long.valueOf(questionId));
				optQuestion.ifPresent(questionBean -> {

					if((latestLastYear > Integer.valueOf(questionBean.getYear()))
							|| (latestLastYear == Integer.valueOf(questionBean.getYear())
									&&("A".equals(latestLastTerm) && "H".equals(questionBean.getTerm())))){
						// 直近の年度、期の6回分に該当しない場合は削除する
						// 【条件】(1)と(2)はOR
						// (1)年度：直近6回分で最も古い年度より前の年度
						// (2)年度：直近6回分で最も古い年度と同じ　AND
						//      期：直近6回分で最も古いものの期が'A'で問題の方が'H'
						removeQuestionIdList.add(String.valueOf(questionBean.getId()));
					}
					
					// ※中止試験があった場合、コード修正が必要
					// 現在年月から過去6回を抽出する処理（破棄）
//					int yearInt = Integer.valueOf(questionBean.getYear());
//					String termStr = questionBean.getTerm();
//					int nowYearInt = Calendar.getInstance().get(Calendar.YEAR);
//					int nowMonthInt = Calendar.getInstance().get(Calendar.MONTH);
//					if(nowYearInt >= 2013 && nowMonthInt > 10) {
//						// 2020春試験（中止）の影響なし
//						if(nowMonthInt < 5) {
//							// (1-4月)春試験：年度後
//							if((nowYearInt - yearInt) > 3) {
//								removeQuestionIdList.add(String.valueOf(questionBean.getId()));
//							}
//						} else if(nowMonthInt > 10) {
//							// (11-12月)春試験：年度前
//							if((nowYearInt - yearInt) > 2) {
//								removeQuestionIdList.add(String.valueOf(questionBean.getId()));
//							}
//						} else if(nowMonthInt < 11) {
//							// (5-10月)秋試験:
//							if(((nowYearInt - yearInt) > 3) || ((nowYearInt - yearInt) == 3) && "H".equals(termStr)) {
//								removeQuestionIdList.add(String.valueOf(questionBean.getId()));
//							}
//						}
//					} else {
//						// 2020春試験（中止）の影響あり
//						if(nowMonthInt < 5) {
//							// (1-4月)春試験：年度後
//							if(((nowYearInt - yearInt) > 4) || ((nowYearInt - yearInt) == 4) && "H".equals(termStr)) {
//								removeQuestionIdList.add(String.valueOf(questionBean.getId()));
//							}
//						} else if(nowMonthInt > 10) {
//							// (11-12月)春試験：年度前
//							if(((nowYearInt - yearInt) > 3) || ((nowYearInt - yearInt) == 3) && "H".equals(termStr)) {
//								removeQuestionIdList.add(String.valueOf(questionBean.getId()));
//							}
//						} else if(nowMonthInt < 11) {
//							// (5-10月)秋試験:
//							if((nowYearInt - yearInt) > 3) {
//								removeQuestionIdList.add(String.valueOf(questionBean.getId()));
//							}
//						}
//					}
				});
			}
			questionIdList.removeAll(removeQuestionIdList);
		}
		
		form.setQuestionList(questionIdList);
		
		return form;
	}

	/**
	 * 自習問題リストをソートする
	 * 
	 * @param selfStudyForm 自習Form(self study form)
	 * @return 自習Form(self study form)
	 */
	@Override
	public SelfStudyForm sortQuestionList(SelfStudyForm form) {
		
		if(form.getSortChecked() != null) {
			List<String> questionIdList = form.getQuestionList();
			
			if(form.getSortChecked().equals(SelfStudyForm.SORT_3_KEY_RANDOM)) {
				// ランダムでソート
			
				Collections.shuffle(questionIdList);
				
				form.setQuestionList(questionIdList);
			} else {
	
				List<QuestionBean> sortQuestionBeanList = new ArrayList<>();
				List<QuestionBean> questionBeanList = questionRepository.findAll();
				for(QuestionBean bean : questionBeanList) {
					if(questionIdList.contains(String.valueOf(bean.getId()))) {
						sortQuestionBeanList.add(bean);
					}
				}
	
				List<QuestionBean> sortedQuestionBeanList = new ArrayList<>();
				if(form.getSortChecked().equals(SelfStudyForm.SORT_1_KEY_LATEST)) {
					// 新しい順でソート
		
			    	sortedQuestionBeanList = sortQuestionBeanList.stream()
			    	        .sorted(Comparator.comparing(QuestionBean::getYear, Comparator.reverseOrder())
			    	        		.thenComparing(QuestionBean::getTerm, Comparator.naturalOrder()))
			    	        .collect(Collectors.toList());
		
				} else if(form.getSortChecked().equals(SelfStudyForm.SORT_2_KEY_PREVIOUS)) {
					// 古い順でソート
					
			    	sortedQuestionBeanList = sortQuestionBeanList.stream()
			    	        .sorted(Comparator.comparing(QuestionBean::getYear, Comparator.naturalOrder())
			    	        		.thenComparing(QuestionBean::getTerm, Comparator.reverseOrder()))
			    	        .collect(Collectors.toList());
			
				}
				
				List<String> sortedQuestionIdList = new ArrayList<>();
				for(QuestionBean bean : sortedQuestionBeanList) {
					sortedQuestionIdList.add(String.valueOf(bean.getId()));
				}
	
				form.setQuestionList(sortedQuestionIdList);
			}
		}
			
		return form;
	}
	
	/**
	 * 特定の問題の自習問題Formを取得する.
	 * 
	 * @param selfStudyForm 自習Form(self study form)
	 * @param number 問題番号(question number)
	 * @return 自習問題Form(self study question form)
	 */
	@Override
	public SelfStudyQuestionForm getQuestion(SelfStudyQuestionForm form, int number) {
		
		SelfStudyQuestionForm selfStudyQuestionForm = getSelfStudyQuestionForm(form, number);
		
		// 回答を語句に変換
		selfStudyQuestionForm.setCorrect(convertAnsweredIdToWord(selfStudyQuestionForm.getCorrect()));
		
		return selfStudyQuestionForm;
	}
	
	/**
	 * 回答処理を行い、特定の問題の自習問題Formを取得する.
	 * 
	 * @param selfStudyForm 自習Form(self study form)
	 * @param number 問題番号(question number)
	 * @return 自習問題Form(self study question form)
	 */
	@Override
	public SelfStudyQuestionForm getQuestionAndAnswer(SelfStudyQuestionForm form, int number) {
		
		// 問題情報をセットする
		SelfStudyQuestionForm selfStudyQuestionForm = getSelfStudyQuestionForm(form, number);
		// タグ情報をセットする
        String questionId = form.getQuestionList().get(number);
        List<String> tagIdList = getQuestionTagList(questionId);
		selfStudyQuestionForm.setQuestionTag(tagIdList);

		// 回答履歴を保存する
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        
		StudentQuestionHistoryBean newSqhBean = new StudentQuestionHistoryBean();
		if(selfStudyQuestionForm.getCorrect().equals(form.getAnswer())) {
			newSqhBean.setCorrectFlg(true);
		} else {
			newSqhBean.setCorrectFlg(false);
		}
		newSqhBean.setQuestionId(Long.valueOf(questionId));
		newSqhBean.setUserId(userId);
		newSqhBean.setUpdateDate(new Date());
		studentQuestionHistoryRepository.save(newSqhBean);
		
		// 履歴保存後に回答を語句に変換
		selfStudyQuestionForm.setCorrect(convertAnsweredIdToWord(selfStudyQuestionForm.getCorrect()));
		
		return selfStudyQuestionForm;
	}
	
    /**
     * 問題タグアイテム取得
     * 
     * @return 問題タグアイテムマップ
     */
	@Override
    public Map<String, String> getQuestionTagSelectedItems() {
        Map<String, String> selectMap = new LinkedHashMap<String, String>();
        selectMap.put(String.valueOf(QuestionTag.QUESTION_TAG_1_TAG_RED.getId()), QuestionTag.QUESTION_TAG_1_TAG_RED.getName());
        selectMap.put(String.valueOf(QuestionTag.QUESTION_TAG_2_TAG_GREEN.getId()), QuestionTag.QUESTION_TAG_2_TAG_GREEN.getName());
        selectMap.put(String.valueOf(QuestionTag.QUESTION_TAG_3_TAG_BLUE.getId()), QuestionTag.QUESTION_TAG_3_TAG_BLUE.getName());
        return selectMap;
	}
	
    /**
     * 回答アイテム取得
     * 
     * @return 回答アイテムマップ
     */
    @Override
    public Map<String,String> getAnswerSelectedItems(){
        Map<String, String> selectMap = new LinkedHashMap<String, String>();
        selectMap.put("1", "ア");
        selectMap.put("2", "イ");
        selectMap.put("3", "ウ");
        selectMap.put("4", "エ");
        return selectMap;
    }
    
    /**
     * 自習用課題作成.
     * @param form 自習Form
     */
    @Override
    public void createSelfTask(SelfStudyForm form) {
    	
    	TaskBean taskBean = new TaskBean();

        // 課題作成者（自身のID）を設定する
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        taskBean.setTeacherId(userId);
        
        // 課題、問題中間情報をBeanに設定する
    	taskBean.clearTaskQuestionBean();
        List<String> questionList = form.getQuestionList();
        if(questionList != null) {
        	int i = 0;
        	for(String questionId : questionList) {
        		
	            TaskQuestionBean taskQuestionBean = new TaskQuestionBean();

	            Optional<QuestionBean> optQuestion = questionRepository.findById(Long.valueOf(questionId));
	            optQuestion.ifPresent(questionBean -> {

		            taskQuestionBean.setQuestionId(questionBean.getId());
	            });
	            taskQuestionBean.setSeqNumber(Long.valueOf(i++));

	            taskBean.addTaskQuestionBean(taskQuestionBean);
        	}
        }
        // 問題数を設定する
        taskBean.setQuestionSize(Long.valueOf(questionList.size()));
        
        // 提示先情報（ユーザ、課題中間情報）をBeanに設定する
    	StudentTaskBean studentTaskBean = new StudentTaskBean();
        studentTaskBean.setUserId(userId);
        
        taskBean.addStudentTaskBean(studentTaskBean);
    
	    // DBに保存する
	    taskBean.setTitle("【自習問題】");
	    taskBean.setDescription("【作成日】" + new SimpleDateFormat("yyyy年MM月dd日(E) H時mm分").format(new Date()));
	    taskBean = taskRepository.save(taskBean);
    }

    /**
     * 問題タグ情報保存.
     * 
     * @param form 自習問題Form
     */
    @Override
    public void saveQuestionTag(SelfStudyQuestionForm form) {

		List<UserBean> userBeanList = new ArrayList<UserBean>();
		// 一旦ユーザー情報を削除
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        String questionId = form.getQuestionList().get(form.getSelectQuestionNumber());
        Optional<UserBean> optUser = userRepository.findById(userId);
        optUser.ifPresent(userBean -> {
        	userBeanList.add(userBean);
        });
    	
    	List<String> tagIdList = form.getQuestionTag();
    	if(tagIdList != null && tagIdList.size() > 0) {
    		// 問題タグあり
    		if(userBeanList.size() > 0) {
	            UserBean userBean = userBeanList.get(0);
	    		// 一旦ユーザー情報を削除
	        	userRepository.delete(userBean);
	            // タグ情報を更新したユーザー情報を登録
	            userBean.updateQuestionTagId(questionId, tagIdList);
	            userRepository.save(userBean);
    		}
    	} else {
    		// 問題タグなし:タグ情報がある場合のみ更新（削除）

    		List<String> tagList = userBeanList.get(0).getQuestionTagList(questionId);
    		if(tagList != null && tagList.size() > 0) {

    			if(userBeanList.size() > 0) {
    	            UserBean userBean = userBeanList.get(0);
		    		// 一旦ユーザー情報を削除
		        	userRepository.delete(userBean);
		            // タグ情報を更新したユーザー情報を登録
		            userBean.updateQuestionTagId(questionId, tagIdList);
		            userRepository.save(userBean);
    			}
    		}
    	}
    }
    
    /**
     * 問題タグをFormにセットする.
     * 
     * @param form 自習問題Form
     * @return 自習問題Form
     */
    private List<String> getQuestionTagList(String questionId) {
    	
    	List<String> list = new ArrayList<String>();
    	
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth.getName();
        
        Optional<UserBean> optUser = userRepository.findById(userId);
        optUser.ifPresent(userBean -> {
			List<String> tagIdList = userBean.getQuestionTagList(questionId);
			if(tagIdList != null && tagIdList.size() > 0) {
				list.addAll(tagIdList);
        	}
        });
        
    	return list;
    }
    
    /**
     * 指定番号の問題情報を取得し、Formにセットする.
     * 
     * @param form 自習問題Form
     * @param number 問題の指定番号
     * @return 自習問題Form
     */
    private SelfStudyQuestionForm getSelfStudyQuestionForm(SelfStudyQuestionForm form, int number) {
    	
		SelfStudyQuestionForm selfStudyQuestionForm = new SelfStudyQuestionForm();
		
		// 自習用の問題情報をセットする
		selfStudyQuestionForm.setQuestionList(form.getQuestionList());
		selfStudyQuestionForm.setSelectQuestionNumber(number);
		
		selfStudyQuestionForm.setAnswer(form.getAnswer());

        String questionId = form.getQuestionList().get(number);
        
		Optional<QuestionBean> optQuestion = questionRepository.findById(Long.valueOf(questionId));
		optQuestion.ifPresent(questionBean -> {
    		// 問題の情報をセットする
			selfStudyQuestionForm.setCorrect(String.valueOf(questionBean.getCorrect()));
			selfStudyQuestionForm.setDivision(questionBean.getDivision());
			selfStudyQuestionForm.setFieldLId(String.valueOf(questionBean.getFieldLId()));
			selfStudyQuestionForm.setFieldMId(String.valueOf(questionBean.getFieldMId()));
			selfStudyQuestionForm.setFieldSId(String.valueOf(questionBean.getFieldSId()));
			selfStudyQuestionForm.setId(String.valueOf(questionBean.getId()));
			selfStudyQuestionForm.setNumber(String.valueOf(questionBean.getNumber()));
			selfStudyQuestionForm.setTerm(questionBean.getTerm());
			selfStudyQuestionForm.setYear(questionBean.getYear());
    	});
		String imagePath = selfStudyQuestionForm.getYear() + "_" + selfStudyQuestionForm.getTerm()
			+ "/" + String.format("%02d", Integer.parseInt(selfStudyQuestionForm.getNumber())) + ".png";
		selfStudyQuestionForm.setImagePath(imagePath);
		
    	// 問題情報文字列を作成し、Formにセットする
    	StringBuffer questionInfoStrBuff = new StringBuffer();
    	questionInfoStrBuff.append(JPCalenderEncoder.getInstance().convertJpCalender(selfStudyQuestionForm.getYear(), selfStudyQuestionForm.getTerm()));
    	
		questionInfoStrBuff.append("期 問" + selfStudyQuestionForm.getNumber());
		selfStudyQuestionForm.setQuestionInfoStr(questionInfoStrBuff.toString());
    	
    	// 問題分野情報文字列を作成し、Formにセットする
		selfStudyQuestionForm.setQuestionFieldInfoStr(
    			FieldLarge.getName(ExamDivisionCode.AP.getName(), Byte.valueOf(selfStudyQuestionForm.getFieldLId())) + "/"
    			+ FieldMiddle.getName(ExamDivisionCode.AP.getName(), Byte.valueOf(selfStudyQuestionForm.getFieldMId())) + "/"
    			+ FieldSmall.getName(ExamDivisionCode.AP.getName(), Byte.valueOf(selfStudyQuestionForm.getFieldSId())));
		
		return selfStudyQuestionForm;
    }
	
    /**
     * 画面用年度マップ取得
     * @return 画面用年度マップ（key:ドロップダウンリストID、value：年度ラベル）
     */
    private Map<String, String> findAllYearMap() {
    	
    	Map<String, String> map = new LinkedHashMap<String, String>();
    	
    	for(QuestionBean questionBean : questionRepository.findDistinctYearAndTerm()) {
    		StringBuffer keyBuff = new StringBuffer();
    		StringBuffer valueBuff = new StringBuffer();
    		// 年度
    		keyBuff.append(questionBean.getYear());
    		
    		String termStr = questionBean.getTerm();
    		// 期
    		if("H".equals(termStr)) {
    			keyBuff.append("H");
    		} else {
    			keyBuff.append("A");
    		}
    		valueBuff.append(JPCalenderEncoder.getInstance().convertJpCalender(questionBean.getYear(), termStr));
    		
    		map.put(keyBuff.toString(), valueBuff.toString());
    	}
    	return map;
    }
    
    /**
     * 画面用大分類マップ取得(Get large  map for screen).
     * @return 画面用大分類マップ（key:ドロップダウンリストID、value：大分類ラベル）
     */
    private Map<String, String> findAllFieldLMap() {
    	
    	Map<String, String> map = new LinkedHashMap<String, String>();

    	EnumSet.allOf(FieldLarge.class)
    	  .forEach(fieldL -> map.put(String.valueOf(fieldL.getId()), fieldL.getName()));
    	
    	return map;
    }
    
    /**
     * 画面用中分類マップ取得(Get middle filed map for screen).
     * @param parentId 大分類ID(large field id)
     * @return 画面用中分類マップ（key:ドロップダウンリストID、value：中分類ラベル）
     */
    private Map<String, String> findAllFieldMMap(String parentId) {

    	Map<String, String> map = new LinkedHashMap<String, String>();
    	if(parentId != null && !parentId.equals("")) {
    		map.putAll(FieldMiddle.getMap(Byte.valueOf(parentId)));
    	}
    	return map;
    }
    
    /**
     * 画面用小分類マップ取得(Get small filed map for screen).
     * @param parentId 中分類ID(middle field id)
     * @return 画面用小分類マップ（key:ドロップダウンリストID、value：小分類ラベル）
     */
    private Map<String, String> findAllFieldSMap(String parentId) {
    	
    	
    	Map<String, String> map = new LinkedHashMap<String, String>();
    	if(parentId != null && !parentId.equals("")) {
    		map.putAll(FieldSmall.getMap(Byte.valueOf(parentId)));
    	}
    	return map;
    }
    
	/**
	 * 回答IDを語句に置き換える(Convert answered id to word).
	 * @param answeredId 回答ID(answered id)
	 * @return 回答語句(answered word)
	 */
	private String convertAnsweredIdToWord(String answeredId) {
		String answeredWord = answeredId;
		switch(answeredId) {
			case "1":
				answeredWord = "ア";
				break;
			case "2":
				answeredWord = "イ";
				break;
			case "3":
				answeredWord = "ウ";
				break;
			case "4":
				answeredWord = "エ";
				break;
			default:
				answeredWord = "";
				break;
		}
		return answeredWord;
	}
	
	/**
	 * 
	 * @param yearAndTermDataList
	 * @param questionBean
	 * @return
	 */
	private boolean containsYearAndTermData(List<YearAndTermData> yearAndTermDataList, QuestionBean questionBean) {
		
		return true;
	}
	
	/**
	 * 年度、期情報クラス
	 * 
	 * @author user-01
	 *
	 */
	@Data
	@AllArgsConstructor
	private class YearAndTermData {
		
		/**
		 * 年度
		 */
		private int year;
		
		/**
		 * 期
		 */
		private String Term;
	}
	
	/**
	 * 正解、不正解情報クラス
	 * @author user01
	 *
	 */
	@Data
	private class RateData {
		
		/**
		 * 正解数
		 */
		private int correctCnt = 0;
		
		/**
		 * 不正解数
		 */
		private int incorrectCnt = 0;
	}
}
