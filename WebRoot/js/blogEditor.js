    //实例化编辑器
    //建议使用工厂方法getEditor创建和引用编辑器实例，如果在某个闭包下引用该编辑器，直接调用UE.getEditor('editor')就能拿到相关的实例
    var ue = UE.getEditor('editor');
	var url = document.URL
	var idxStr = "blogId="
	var post = url.substr(url.indexOf(idxStr) + idxStr.length);
	var postUrl = "http://970655147s-pc:8080/HXBlog/post/" + post + ".html";
	
	ue.ready(function() {
	 	var resp = $.ajax({url:postUrl, async:false});
		ue.execCommand('insertHtml', resp.responseText)
	})	
	
	// 提交任务
	function submit() {
		var revisedContent = ue.getAllHtml()
		$.ajax({ url: "/HXBlog/blogReviseAction", type : "post",
				data:{
					revised : revisedContent,
					path : post
				},
				success: function(){
		       
		        }
		});
	}