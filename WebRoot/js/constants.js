    // -------------------- ���ݽṹ���--------------------------
	// jsʵ��StringBuilder
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
	function Blog(id, title, tags, content) {
		this.id = id
		this.title = title
		this.tags = tags
		this.content = content
	}
	Blog.prototype.toString = function() {
		var sb = new StringBuilder()
		sb.append("{ ")
		sb.append("id : \"")
		sb.append(this.id)		
		sb.append("\", title : \"")
		sb.append(this.title)
		sb.append("\", tags : \"")
		sb.append(this.tags)		
		sb.append("\", content : \"")
		sb.append(this.content)		
		sb.append("\" }")
		
		return sb.toString(EMPTY_STR)
	}
	Blog.prototype.getBlogObj = function() {
		var blogObj = {
				"id" : this.id,
				"title" : this.title,
				"tags" : this.tags,
				"content" : this.content
		}
		
		return blogObj
	}	
	
	// commentBean
	function Comment(userName, email, imageIdx, comment) {
		this.userName = userName
		this.email = email
		this.imageIdx = imageIdx
		this.comment = comment
	}
	Comment.prototype.toString = function() {
		var sb = new StringBuilder()
		sb.append("{ ")
		sb.append("userName : \"")
		sb.append(this.userName)		
		sb.append("\", email : \"")
		sb.append(this.email)
		sb.append("\", imageIdx : \"")
		sb.append(this.imageIdx)		
		sb.append("\", comment : \"")
		sb.append(this.comment)		
		sb.append("\" }")
		
		return sb.toString(EMPTY_STR)
	}
	Comment.prototype.getBlogObj = function() {
		var blogObj = {
				"userName" : this.userName,
				"email" : this.email,
				"imageIdx" : this.imageIdx,
				"comment" : this.comment
		}
		
		return blogObj
	}		
	
	
	// ����
	var EMPTY_STR = ""
	var NULL = "null"
	var UNDEFINED = "undefined"
	var HTML = ".html"
	var tagSep = ","
	var maxTagNum = 10
	var maxTagLength = 7
	var specCharReg = "[!-/ | :-@ | {-~ | \\[ /\\]^����]"
//	var haveNoPrevBlogIdx = -1
//	var haveNoNextBlogIdx = -2
	
	// ͨ�õķ���
	// �жϸ�����name�Ƿ�����regExp
	function isMatch(regExp, nickName){
	    var reg = new RegExp(regExp);
	    return reg.test(nickName);
	}
	
	// ��ȡstr�����һ��sep�ָ���֮�������
	function getStrAfterLastSep(str, sep) {
		var lastIdx = str.lastIndexOf(sep)
		if(lastIdx >= 0) {
			return str.substr(lastIdx + sep.length);
		} else {
			return null;
		}
	}	
	
	// У��������ַ����Ƿ�Ϊ��
	function isEmpty(content) {
		return (typeof(content) == UNDEFINED) || (content == null) || (content.length == 0) || (content.trim() == EMPTY_STR)
	}
	
    // У��title
    function validateTitle(title, titleStr, noticePath) {
    	if(isEmpty(title) ) {
    		$(noticePath).html(titleStr + " can't be empty !")
    		return false
    	}
    	if(isMatch(specCharReg, title)) {
    		$(noticePath).html(titleStr + " can't contains spec character [eg : ! - /] !")
    		return false
    	}    	
    	
    	return true
    }
    
    // У����������
    function validateContent(content, contentStr, noticePath) {
    	if(isEmpty(content) ) {
    		$(noticePath).html(contentStr + " can't be empty !")
    		return false
    	}
    	
    	return true
    }	
    
	