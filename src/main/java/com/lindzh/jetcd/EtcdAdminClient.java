package com.lindzh.jetcd;

import java.util.List;

public interface EtcdAdminClient {

	public List<EtcdMember> members();
	
	public EtcdMember addMembers(List<String> members);
	
	public boolean delMember(String id);
	
	public EtcdMember setMembers(String id,List<String> members);
}
