
	//ʵ�����༭��
    //����ʹ�ù�������getEditor���������ñ༭��ʵ���������ĳ���հ������øñ༭����ֱ�ӵ���UE.getEditor('editor')�����õ���ص�ʵ��
	var tagsPath = "div#tags span[class$='input-group-addon']"	
	var tagInputPath = "input#tagInput"    	
	var tagNoticePath = "span#tagNotice"
	var submitNoticePath = "span#submitNotice"	
		
    var ue = UE.getEditor('editor')
	var url = document.URL
	var post = getBlogId(url, "blogId=", "&")
	var isRevise = equalsIgnoreCase("true", getBlogId(url, "revise=", "&"))
	var postUrl = EMPTY_STR
	if(post != EMPTY_STR) {
		postUrl = "/HXBlog/action/blogGetAction?blogId=" + post
	}
	
	// �����Ҫ���ز���, ����ز���
	ue.ready(function() {
		if(postUrl != EMPTY_STR) {
		 	var resp = $.ajax({url:postUrl, async:false});
		 	resp = JSON.parse(resp.responseText)
			ue.execCommand('insertHtml', resp.blog.content)
			$("#title").val(resp.blog.title)
			
			var tags = eval(resp.blog.tags)
			for(i=0; i<tags.length; i++) {
				appendTag(tagsPath, tags[i])
			}
		}
	})
	
    // -------------------- ����Ҫ���¼� --------------------------
 	// <span class="input-group-addon btn btn-default" style="color:blue" >tags</span>
	// ���һ����ǩ
    $(tagInputPath).blur(function () {
    	$(tagNoticePath).html(EMPTY_STR)    	
    	// include a "tags" span
    	var tag = $(tagInputPath).val().trim()
   
    	if(validateTag(tag, tagNoticePath) ) {
    		if(validateTags(tagsPath, tag, tagNoticePath)) {
        		appendTag(tagsPath, tag)
        		$(tagInputPath).val(EMPTY_STR)		
    		}	
		}
    })
    
    // У��, �ύ
    $("#submitBtn").click(function() {
    	var title = $("input#title").val().trim()
    	var checkCode = $("#checkCode").val().trim()
    	var content = ue.getContent()
    	$(submitNoticePath).html(EMPTY_STR)
    	
    	if(validateTitle(title, "title", submitNoticePath)) {
    		if(validateTitle(checkCode, "checkCode", submitNoticePath)) {
	    		if(validateContent(content, "content", submitNoticePath)) {
	    			freshCheckCode()
	    			ts += 1
	    			var blogObj = new Blog(post, title, getTags(tagsPath), checkCode, content)
	    			var postUrl = EMPTY_STR
	    			if(! isRevise) {
	    				postUrl = "/HXBlog/action/blogPublishAction"
	    			} else {
	    				postUrl = "/HXBlog/action/blogReviseAction"
	    			}
	    			
					$.ajax({
						url: postUrl, type : "post",
						data : blogObj.getObj(),
						success : function(data){
							data = JSON.parse(data)
							
							$("#respMsg").html(data.msg)
							$("#myModal").modal()
							if(data.isSuccess) {
						    	$("input#title").val("")
						    	ue.setContent("")
						    	console.log($("#tags").find("span.btn").length )
						    	$("#tags").find("span.btn").remove()
						    	$("#tagInput").val("")
							}
							$("#checkCode").val("")
				        }
					});    			
	    		}
    		}
    	}
    })
    
    // ˢ����֤��
    var ts = 0
    function freshCheckCode() {
		$("#checkCodeImg").attr("src", "/HXBlog/action/blogCheckCodeAction?ts=" + ts )
		ts += 1
	}
	$("#checkCodeImg").click(freshCheckCode)
	$("#freshCheckCode").click(freshCheckCode)
//	// �ύ����
//	function submit() {
//		if(postUrl != EMPTY_STR) {
//			var revisedContent = ue.getContent()
//			$.ajax({ url: "/HXBlog/action/blogReviseAction", type : "post",
//					data:{
//						revised : revisedContent,
//						path : post
//					},
//					success: function(){
//						console.log("fgz")
//			        }
//			});
//		}
//	}    
    
    // �󶨼���д���Ͱ�ť���¼�
    	// ������޸�ҳ��, ����¸����ڵ�href
    	// ���� ��ʾ�������ҳ��, ˢ�¸�����
	$("#goOnBlog").click(function() {
		if(isRevise) {
			parent.location.href = "/HXBlog/#!/blogPublishAction"
		} else {
			parent.location.reload()
		}
//		console.log(parent.location.href)
//		document.URL = location.href
//		document.URL =  "/HXBlog/#!/blogPublishAction"
//		location.reload()
	})
    						
    // ��ʼ��������ť�ĵ���¼�
    $(tagsPath).click(removeThis)

    // -------------------- ���߷��� --------------------------
	// ��ȡ���͵�����
	function getBlogId(url, idStartIdxStr, idEndIdxStr) {
		var idStartIdx = url.indexOf(idStartIdxStr)
		var post = EMPTY_STR;
		if(idStartIdx >= 0) {
			var idEndIdx = url.indexOf(idEndIdxStr, idStartIdx + idStartIdxStr.length)
			if(idEndIdx > idStartIdx) {
				post = url.substring(idStartIdx + idStartIdxStr.length, idEndIdx)
			} else {
				post = url.substring(idStartIdx + idStartIdxStr.length)
			}
		}
		
		return post
	}
	
	// �жϸ�����tags[Elements] ���Ƿ���ڸ�����tag
	function isTagExists(tags, tag) {
		var isExists = false
//    	tags.each(function() {
//    		if($(this).text() == tag) {
//    			isExists = true
//    		}
//    	})
    	for(i=0; i<tags.length; i++) {
    		if(equalsIgnoreCase(tags.get(i).innerText.trim(), tag)) {
    			isExists = true
    			break 
    		}
    	}
		
    	return isExists
	}
	
	// �ж������ַ����Ƿ���ͬ
	function equalsIgnoreCase(str01, str02) {
		return str01.toUpperCase() == str02.toUpperCase()
	}
	
    // ΪtagPath��Ӧ�����һ��Ԫ�����һ��Ԫ��
    function appendTag(tagsPath, tag) {
    	$(tagsPath).last().after("<span class='btn btn-default input-group-addon' style='color:blue' >" + tag + "</span>")
		$(tagsPath).last().click(removeThis)
    }
    
    // �Ƴ���ǰԪ��
    function removeThis() {
        $(this).remove()
    }
    
    // У��tag
    function validateTag(tag, noticePath) {
    	if(tag == EMPTY_STR) {
//    		$(noticePath).html("tag can't be empty !")
    		return false
    	}
    	if(tag.length >= maxTagLength) {
    		$(noticePath).html("tag is too long !")
    		return false
    	}
    	if(isMatch(specCharReg, tag)) {
    		$(noticePath).html("tag can't contains spec character [eg : ! - /] !")
    		return false
    	}
    		
    	return true
    }
    
    // У��tags
    function validateTags(tagsPath, tag, noticePath) {
    	var tags = $(tagsPath)
    	// "tags" include another "tags span", so use ">"
    	if(tags.length > maxTagNum) {
    		$(noticePath).html("tag is to much !")    	
    		return false
    	}    
    	var isExists = isTagExists(tags, tag)
    	if(isExists) {
    		$(noticePath).html("this tag already exists !")    	
    		return false
    	}
    	
    	return true
    }
    
    // ��ȡ���еı�ǩ
    function getTags(tagsPath) {
    	var tags = $(tagsPath)
    	var sb = new StringBuilder()
    	for(i=1; i<tags.length; i++) {
    		sb.append(tags.get(i).innerText)
    	}
    	
    	return sb.toString(tagSep)
    }
    
	// -------------------- editorCtrl --------------------------
//	(function() {
//		  var app = angular.module('blogEditor', ['ngSanitize'])
//		  
//		  // ��ȡ������Ϣ��ctrl
//		  app.controller('editorCtrl', function($scope, $http) {
//			  
//		  });  
//		  
//	}).call(this);	
	