package jp.ac.ems.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import jp.ac.ems.bean.QuestionBean;
import jp.ac.ems.bean.TaskBean;

/**
 * 問題用リポジトリ(question repository).
 * @author tejc999999
 */
public interface QuestionRepository extends
                                    JpaRepository<QuestionBean, Long> {

	/**
	 * 年度、期を指定した全問題取得(get all question by specifying the year and term).
	 * @param year 年度(ex).R01)
	 * @param term 期（'A' or 'H')
	 * @return 問題Beanリスト
	 */
	List<QuestionBean> findAllByYearAndTerm(String year, String term);

	/**
	 * 大分類を指定した全問取得(get all question by specifying the large field).
	 * @param field_l_id 大分類(large field)
	 * @return 問題Beanリスト
	 */
	List<QuestionBean> findAllByFieldLId(Byte field_l_id);
	
	/**
	 * 中分類を指定した全問取得(get all question by specifying the middle field).
	 * @param field_m_id 中分類(middle field)
	 * @return 問題Beanリスト
	 */
	List<QuestionBean> findAllByFieldMId(Byte field_m_id);

	/**
	 * 小分類を指定した全問取得(get all question by specifying the small field).
	 * @param field_s_id 小分類(small field)
	 * @return 問題Beanリスト
	 */
	List<QuestionBean> findAllByFieldSId(Byte field_s_id);

	  /**
	  * 年度、期、タイトルと説明文に検索文字列が含まれることを条件とした取得.
	  * 
	  * @return 問題Beanリスト
	  */
//	 List<QuestionBean> findDistinctYearAndTerm();
	 @Query("select distinct new jp.ac.ems.bean.QuestionBean(q.year, q.term) from QuestionBean q")
	 List<QuestionBean> findDistinctYearAndTerm();
	
//    /**
//     * タイトルと説明文に検索文字列が含まれることを条件とした取得.
//     * 
//     * @param searchTitleStr タイトル対象検索文字列
//     * @param searchDescriptionStr 説明文対象検索文字列
//     * @return 問題Beanリスト
//     */
//    List<QuestionBean> findByTitleLikeOrDescriptionLike(
//            String searchTitleStr, String searchDescriptionStr);   
}
