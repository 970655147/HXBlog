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
	
	// 常量
	var EMPTY_STR = ""
	var NULL = "null"
	var UNDEFINED = "undefined"
	var HTML = ".html"
	var tagSep = ","
	var maxTagNum = 10
	var maxTagLength = 7
//	var haveNoPrevBlogIdx = -1
//	var haveNoNextBlogIdx = -2
	