    //ʵ�����༭��
    //����ʹ�ù�������getEditor���������ñ༭��ʵ���������ĳ���հ������øñ༭����ֱ�ӵ���UE.getEditor('editor')�����õ���ص�ʵ��
    var ue = UE.getEditor('editor');
	var url = document.URL
	var idxStr = "blogId="
	var post = url.substr(url.indexOf(idxStr) + idxStr.length);
	var postUrl = "http://970655147s-pc:8080/HXBlog/post/" + post + ".html";
	
	ue.ready(function() {
	 	var resp = $.ajax({url:postUrl, async:false});
		ue.execCommand('insertHtml', resp.responseText)
	})	
	
	// �ύ����
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