package io.github.icodegarden.beecomb.master.manager;

import com.github.pagehelper.Page;

import io.github.icodegarden.beecomb.master.pojo.query.ClusterNodeQuery;
import io.github.icodegarden.beecomb.master.pojo.view.ClusterNodeVO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface ClusterNodeManager {

	Page<ClusterNodeVO> pageNodes(ClusterNodeQuery query);
}
