
	//实例化编辑器
    //建议使用工厂方法getEditor创建和引用编辑器实例，如果在某个闭包下引用该编辑器，直接调用UE.getEditor('editor')就能拿到相关的实例
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
	
	// 如果需要加载博客, 则加载博客
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
	
    // -------------------- 绑定需要的事件 --------------------------
 	// <span class="input-group-addon btn btn-default" style="color:blue" >tags</span>
	// 添加一个标签
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
    
    // 校验, 提交
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
    
    // 刷新验证码
    var ts = 0
    function freshCheckCode() {
		$("#checkCodeImg").attr("src", "/HXBlog/action/blogCheckCodeAction?ts=" + ts )
		ts += 1
	}
	$("#checkCodeImg").click(freshCheckCode)
	$("#freshCheckCode").click(freshCheckCode)
//	// 提交任务
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
    
    // 绑定继续写博客按钮的事件
    	// 如果是修改页面, 则更新父窗口的href
    	// 否则 表示添加帖子页面, 刷新父窗口
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
    						
    // 初始化各个按钮的点击事件
    $(tagsPath).click(removeThis)

    // -------------------- 工具方法 --------------------------
	// 获取博客的名称
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
	
	// 判断给定的tags[Elements] 中是否存在给定的tag
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
	
	// 判断两个字符串是否相同
	function equalsIgnoreCase(str01, str02) {
		return str01.toUpperCase() == str02.toUpperCase()
	}
	
    // 为tagPath对应的最后一个元素添加一个元素
    function appendTag(tagsPath, tag) {
    	$(tagsPath).last().after("<span class='btn btn-default input-group-addon' style='color:blue' >" + tag + "</span>")
		$(tagsPath).last().click(removeThis)
    }
    
    // 移除当前元素
    function removeThis() {
        $(this).remove()
    }
    
    // 校验tag
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
    
    // 校验tags
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
    
    // 获取所有的标签
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
//		  // 获取简历信息的ctrl
//		  app.controller('editorCtrl', function($scope, $http) {
//			  
//		  });  
//		  
//	}).call(this);	
	