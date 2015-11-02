    // -------------------- 数据结构相关--------------------------
	// js实现StringBuilder
	function StringBuilder() {
		this._stringArray = new Array();
	}
	StringBuilder.prototype.append = function(str){
		this._stringArray.push(str);
	}
	StringBuilder.prototype.toString = function(sep){
		return this._stringArray.join(sep);
	}
	
	// blogBean
	function Blog(title, tags, content) {
		this.title = title
		this.tags = tags
		this.content = content
	}
	Blog.prototype.toString = function() {
		var sb = new StringBuilder()
		sb.append("{ ")
		sb.append("title : \"")
		sb.append(this.title)
		sb.append("\", tags : \"")
		sb.append(this.tags)		
		sb.append("\", content : \"")
		sb.append(this.content)		
		sb.append("\" }")
		
		return sb.toString(NULL)
	}
	Blog.prototype.getBlogObj = function() {
		var blogObj = {
				"title" : this.title,
				"tags" : this.tags,
				"content" : this.content
		}
		
		return blogObj
	}	
	
    // -------------------- 业务相关--------------------------
	// 常量
	var NULL = ""
	var HTML = ".html"
	var tagSep = ","
	var maxTagNum = 10
	var maxTagLength = 7
		
	//实例化编辑器
    //建议使用工厂方法getEditor创建和引用编辑器实例，如果在某个闭包下引用该编辑器，直接调用UE.getEditor('editor')就能拿到相关的实例
    var ue = UE.getEditor('editor')
	var url = document.URL
	var post = getBlogId(url, "blogId=", "&")
	var isRevise = equalsIgnoreCase("true", getBlogId(url, "revise=", "&"))
	var postUrl = NULL
	if(post != NULL) {
		postUrl = "/HXBlog/post/" + post + HTML
	}
	
	// 如果需要加载博客, 则加载博客
	ue.ready(function() {
		if(postUrl != NULL) {
		 	var resp = $.ajax({url:postUrl, async:false});
			ue.execCommand('insertHtml', resp.responseText)
		}
	})
	
	// 提交任务
	function submit() {
		if(postUrl != NULL) {
			var revisedContent = ue.getContent()
			$.ajax({ url: "/HXBlog/action/blogReviseAction", type : "post",
					data:{
						revised : revisedContent,
						path : post
					},
					success: function(){
			       
			        }
			});
		}
	}
	
    // -------------------- 绑定需要的事件 --------------------------
 	// <span class="input-group-addon btn btn-default" style="color:blue" >tags</span>
	// 添加一个标签
    $("input#tagInput").blur(function () {
    	$("span#tagNotice").html("")    	
    	// include a "tags" span
    	var tagsPath = "div#tags span[class$='input-group-addon']"
    	var tagNoticePath = "span#tagNotice"
    	var tagInputPath = "input#tagInput"
    	var tag = $(tagInputPath).val().trim()
   
    	if(validateTag(tag, tagNoticePath) ) {
    		if(validateTags(tagsPath, tag, tagNoticePath)) {
        		appendTag(tagsPath, tag)
        		$(tagInputPath).val(NULL)		
    		}	
		}
    })
    
    // 校验, 提交
    $("#submitBtn").click(function() {
    	var submitNoticePath = "span#submitNotice"
        var tagsPath = "div#tags span[class$='input-group-addon']"
    	var title = $("input#title").val().trim()
    	var content = ue.getContent()
    	$(submitNoticePath).html(NULL)
    	
    	if(validateTitle(title, submitNoticePath)) {
    		if(validateContent(content, submitNoticePath)) {
    			var blogObj = new Blog(title, getTags(tagsPath), content)
    			if(! isRevise) {
    				$.ajax({
    					url: "/HXBlog/action/blogPublishAction", type : "post",
    					data : blogObj.getBlogObj(),
    					success : function(){
    			       
    			        }
    				});
    			} else {
    				
    			}
    		}
    	}
    })
    
    // 初始化各个按钮的点击事件
    $("div#tags span[class$='btn-default']").click(removeThis)

    // -------------------- 工具方法 --------------------------
	// 获取博客的名称
	function getBlogId(url, idStartIdxStr, idEndIdxStr) {
		var idStartIdx = url.indexOf(idStartIdxStr)
		var post = NULL;
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
	
    // 校验title
    function validateTitle(title, noticePath) {
    	if(title == NULL) {
    		$(noticePath).html("title can't be empty !")
    		return false
    	}
    	
    	return true
    }
    
    // 校验输入内容
    function validateContent(content, noticePath) {
    	if(content == NULL) {
    		$(noticePath).html("content can't be empty !")
    		return false
    	}
    	
    	return true
    }
    
    // 校验tag
    function validateTag(tag, noticePath) {
    	if(tag == NULL) {
//    		$(noticePath).html("tag can't be empty !")
    		return false
    	}
    	if(tag.length >= maxTagLength) {
    		$(noticePath).html("tag is too long !")
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
	