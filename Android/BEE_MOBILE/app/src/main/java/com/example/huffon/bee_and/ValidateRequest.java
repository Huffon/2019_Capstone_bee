package com.example.huffon.bee_and;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Huffon on 5/8/2019.
 */

public class ValidateRequest extends StringRequest {
    final static private String URL = "http://18.191.251.171/user/validate";
    private Map<String, String> parameters;

    public ValidateRequest(String userID, Response.Listener<String> listener){
        // 지정 URL에 POST 방식으로 파라미터들을 전송
        super(Method.POST, URL, listener, null);
        parameters = new HashMap<>();
        parameters.put("userID", userID);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return parameters;
    }
}