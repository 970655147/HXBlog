// ��ȡstr�����һ��sep�ָ���֮�������
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

  // ����ng-view��·��
    app.config([
     '$routeProvider', '$locationProvider', function($routeProvider, $locationProvider) {
      // ��һ������Ҫ���� 
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
  
  // ���ƻ�ȡ������Ϣ [����, �ӱ���, �������]
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
  
  // ��ȡ������ǩ
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
  
  // �鿴���ӵĿ�����
  	// $routeParams.postPath.length ��ָ/post/path, path�ĳ���
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
			 $("#post").html(data.blog.content)
			 $("#blogWarnning").hide()
			 if(data.isLogin) {
				 $("#reviseBtn").html(data.reviseBtn)
				 $("#deleteBtn").html(data.deleteBtn) 
				 $("#reviseBtn").attr("class", "btn btn-default")
				 $("#deleteBtn").attr("class", "btn btn-default")
			 } else {
				 $("#reviseBtn").remove()
				 $("#deleteBtn").remove()
			 }
			 
			 // ɾ�����Ӱ�ť
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
//			// ��ҳ��ť�¼�
//			$("#goHome").click(function() {
//				 location = "/HXBlog/"
//			})
			 
			// ��һҳ��ť���¼�
			 $("div[data='prevBlog']").click(function() {
				 if(typeof(data.prevBlogId) != UNDEFINED) {
					 location = "/HXBlog/#!/post/" + data.prevBlogId + "," + idAndTag[1]	 
				 } else {
					 $("#warnning").html("have no prev blog !")
					 $("#blogWarnning").show()
				 }
			 })
			 
			 // ��һҳ��ť�¼�
	 		 $("[data='nextBlog']").click(function() {
	 			if(typeof(data.nextBlogId) != UNDEFINED) {
	 				location = "/HXBlog/#!/post/" + data.nextBlogId + "," + idAndTag[1]
	 			} else {
	 				 $("#warnning").html("have no next blog !")
	 				 $("#blogWarnning").show()
	 			}
			 })
			
		  })		  
	  }
  });
  
  // �������µ�controller
//  app.controller('publishCtrl', function($scope, $http) {
//  });  
  
  // ��ȡ������Ϣ��ctrl
  app.controller('resumeCtrl', function($scope, $http) {
    return $http.get("/HXBlog/action/resumeConfigAction").success(function(data) {
      return $scope.resume = data;
    });
  });  
  
}).call(this);
