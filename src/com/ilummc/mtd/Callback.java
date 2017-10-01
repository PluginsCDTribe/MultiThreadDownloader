package com.ilummc.mtd;

import java.util.HashMap;

public interface Callback {
	void fail(HashMap<String, Object> map);

	void success(HashMap<String, Object> map);
}
