(function() {
  var app = angular.module('blog', ['ngSanitize'])

  // 控制ng-view的路由
    app.config([
     '$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {
      // 这一步必须要加上 
      $locationProvider.html5Mode(false).hashPrefix('!');
      return $routeProvider.when("/", {
        templateUrl: "partials/blogList.html"
      }).when("/blogPublishAction", {
        templateUrl: "partials/publishBlog.html"
      }).when("/tag/:tag", {
        templateUrl: "partials/blogList.html"
      }).when("/resume", {
        templateUrl: "partials/resume.html"
      }).when("/post/:postPath", {
        templateUrl: "partials/post.html"
      });
    }
  ])
  
  // 控制获取顶部信息 [标题, 子标题, 相关连接]
  app.controller('headerCtrl', function($scope, $http, $location) {
//    return $http.get("config.json")
	  return $http.get("/HXBlog/action/blogConfigAction").success(function(data) {
	      var getState = function(path) {
		      var lastStr = getStrAfterLastSep(path, "/")
		      if (lastStr === "resume") {
		    	  return "Resume"
		      } else if(lastStr === "blogPublishAction") {
		    	  return "Publish"
		      } else {
		          return "Blog"
		      }
	      };
	      
	      $scope.state = getState($location.path())
	      $scope.config = data
	      return $scope.$on("$locationChangeSuccess", function(event, newLoc, oldLoc) {
	         $scope.state = getState($location.path())
	      })
	  })
  })
  
  // 获取各个标签
  app.controller('tagListCtrl', function($scope, $http, $routeParams) {
	  var curTag = null
      if (($routeParams.tag != null) && $routeParams.tag.length !== 0) {
	    curTag = $routeParams.tag
      } else {
      	curTag = "all"
      }
	  var blogListReq = "/HXBlog/action/blogListAction" + "?tag=" + curTag

	  return $http.get(blogListReq).success(function(data) {
		  var tags = data.tagList
		  for(i=0; i<tags.length; i++) {
			  tags[i].href = "#!/tag/" + tags[i].text
		  }
		  
		  $scope.tagList = data.tagList
		  $scope.blogList = data.blogList
		  $scope.currentTag = {
				  "text" : data.curTag,
				  "href" : "#!/tag/" + data.curTag
		  }
		  $scope.tagSep = tagSep;
//		  console.log(data.blogList)
	  })
  });
  
  // 查看帖子的控制器
  	// $routeParams.postPath.length 是指/post/path, path的长度
  app.controller('postCtrl', function($scope, $http, $routeParams) {
	  var path = NULL
      if (($routeParams.postPath != null) && $routeParams.postPath.length !== 0) {
    	  path = $routeParams.postPath
      }
	  var idAndTag = path.split(tagSep)
	  if(idAndTag.length == 2) {
		  var blogReq = "/HXBlog/action/blogGetAction?blogId=" + idAndTag[0] + "&tag=" + idAndTag[1]

		  return $http.get(blogReq).success(function(data) {
//			 console.log(data)
			 $scope.postId = data.blog.id
			 $scope.title = data.blog.title
			 $scope.tagList = data.blog.tags
			 $scope.good = data.blog.good
			 $scope.notGood = data.blog.notGood
			 $scope.visited = data.blog.visited
			 var sensed = data.sense
			 
			 $("#post").html(data.blog.content)
			 $("#blogWarnning").hide()
			 if(typeof(data.user) != UNDEFINED) {
				 $("#userName").val(data.user.name)
				 $("#email").val(data.user.email)
				 $("#imageIdx").find("option[id='"+ data.user.imageIdx +"']").attr("selected", true)
			 }
			 
			 if(data.isLogin) {
				 $("#reviseBtn").html(data.reviseBtn)
				 $("#deleteBtn").html(data.deleteBtn) 
				 $("#reviseBtn").attr("class", "btn btn-default")
				 $("#deleteBtn").attr("class", "btn btn-default")
			 } else {
				 $("#reviseBtn").remove()
				 $("#deleteBtn").remove()
			 }
			 
			 // 删除帖子按钮
			$("#deleteAction").click(function() {
				var postUrl = "/HXBlog/action/blogDeleteAction"
				var blogObj = new Blog($scope.postId, null, null, null)
				$.ajax({
					url: postUrl, type : "post",
					data : blogObj.getBlogObj(),
					success : function(data){
						data = JSON.parse(data)
						
						$("#respMsg").html(data.msg)
						$("#myModal").modal()
			        }
				});    
			})					 
//			// 首页按钮事件
//			$("#goHome").click(function() {
//				 location = "/HXBlog/"
//			})
			 
			// 上一页按钮的事件
			 $("div[data='prevBlog']").click(function() {
				 if(typeof(data.prevBlogId) != UNDEFINED) {
					 location = "/HXBlog/#!/post/" + data.prevBlogId + "," + idAndTag[1]	 
				 } else {
					 $("#warnning").html("have no prev blog !")
					 $("#blogWarnning").show()
				 }
			 })
			 
			 // 下一页按钮事件
	 		 $("[data='nextBlog']").click(function() {
	 			if(typeof(data.nextBlogId) != UNDEFINED) {
	 				location = "/HXBlog/#!/post/" + data.nextBlogId + "," + idAndTag[1]
	 			} else {
	 				 $("#warnning").html("have no next blog !")
	 				 $("#blogWarnning").show()
	 			}
			 })
			
			 // 顶踩按钮
			 $("dl[dataId='btnSense']").click( function() {
				 if(! sensed) {
					 sensed = true;
					 var postUrl = "/HXBlog/action/blogSenseAction"
					 var sense = $(this).attr("data")
					 var cur = parseInt($(this).find("dd").html() )
					 $(this).find("dd").html(cur + 1)
						$.ajax({
							url: postUrl, type : "post",
							data : {
								"blogId" : data.blog.id,
								"sense" : sense
							},
							success : function(data){
					        }
						});    
				 } else {
					 console.log("you have already click good / notGood !")
				 }
			})
			
			// 常量
			var submitNoticePath = "#submitNotice"
				
			// 校验, 提交评论
			$("#submitBtn").click(function() {
		    	$(submitNoticePath).html(EMPTY_STR)
		    	var userName = $("#userName").val().trim()
		    	var comment = $("#comment").val()
		    	var imageIdx = $("#imageIdx").find("option[selected='selected']").attr("id")
		    	if(validateTitle(userName, "userName", submitNoticePath)) {
		    		if(validateContent(comment, "comment", submitNoticePath)) {
						var comment = new Comment(userName, $("#email").val().trim(), imageIdx, comment )
		    			var postUrl = "/HXBlog/action/blogCommentAction"
		    			
						$.ajax({
							url: postUrl, type : "post",
							data : comment.getBlogObj(),
							success : function(data){
								data = JSON.parse(data)
								
								$("#submitCommentMsg").html(data.msg)
								$("#submitCommentModal").modal()
					        }
						});    			
		    		}
		    	}
			})
			 
		  })		  
	  }
  });
  
  // 发布文章的controller
//  app.controller('publishCtrl', function($scope, $http) {
//  });  
  
  // 获取简历信息的ctrl
  app.controller('resumeCtrl', function($scope, $http) {
    return $http.get("/HXBlog/action/resumeConfigAction").success(function(data) {
      return $scope.resume = data;
    });
  });  
  
}).call(this);
