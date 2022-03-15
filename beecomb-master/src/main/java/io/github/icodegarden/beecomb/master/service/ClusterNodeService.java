package io.github.icodegarden.beecomb.master.service;

import com.github.pagehelper.Page;

import io.github.icodegarden.beecomb.master.pojo.query.ClusterNodeQuery;
import io.github.icodegarden.beecomb.master.pojo.view.ClusterNodeVO;

/**
 * 
 * @author Fangfang.Xu
 *
 */
public interface ClusterNodeService {

	Page<ClusterNodeVO> pageNodes(ClusterNodeQuery query);
}
