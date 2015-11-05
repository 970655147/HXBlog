// 获取str中最后一个sep分隔符之后的数据
function getStrAfterLastSep(str, sep) {
	var lastIdx = str.lastIndexOf(sep)
	if(lastIdx >= 0) {
		return str.substr(lastIdx + sep.length);
	} else {
		return null;
	}
}

(function() {
  var app = angular.module('blog', ['ngSanitize'])

  // 控制ng-view的跳转
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
			  if(tags[i].text == curTag) {
				  $scope.currentTag = tags[i]
			  }
		  }
		  
		  $scope.tagList = data.tagList
		  $scope.blogList = data.blogList
//		  console.log(data.blogList)
	  })
  });
  
  // 查看帖子的控制器
  app.controller('postCtrl', function($scope, $http, $routeParams) {
	  var path = NULL
      if (($routeParams.postPath != null) && $routeParams.postPath.length !== 0) {
    	  path = $routeParams.postPath
      }
	  var blogReq = "/HXBlog/action/blogGetAction?blogId=" + path
	  
	  return $http.get(blogReq).success(function(data) {
//		 console.log(data)
		 $scope.postId = data.blog.id
		 $("#post").html(data.blog.content)
		 if(data.isLogin) {
			 $("#reviseBtn").html(data.reviseBtn)
			 $("#deleteBtn").html(data.deleteBtn) 
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
//		// 首页按钮事件
//		$("#goHome").click(function() {
//			 location = "/HXBlog/"
//		})
		 
		// 上一页按钮的事件
		 $("div[data='prevBlog']").click(function() {
			 console.log("df")
		 })
		 
		 // 下一页按钮事件
 		 $("#nextBlog").click(function() {
			 
		 })
		
	  })
  });
  
  // 发布文章的controller
  app.controller('publishCtrl', function($scope, $http) {
  });  
  
  // 获取简历信息的ctrl
  app.controller('resumeCtrl', function($scope, $http) {
    return $http.get("/HXBlog/action/resumeConfigAction").success(function(data) {
      return $scope.resume = data;
    });
  });  
  
}).call(this);
