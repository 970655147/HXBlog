// 获取str中最后一个sep分隔符之后的数据
function getStrAfterLastSep(str, sep) {
	var lastIdx = str.lastIndexOf(sep)
	if(lastIdx > 0) {
		return str.substr(lastIdx + sep.length);
	} else {
		return null;
	}
}


(function() {
  var app = angular.module('blog', ['ngSanitize'])

  // 控制ng-view的跳转
    app.config([
    '$routeProvider', function($routeProvider) {
      return $routeProvider.when("/", {
        templateUrl: "partials/blogList.html"
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
	  return $http.get("/HXBlog/blogConfigAction").success(function(data) {
      var getState = function(path) {
         var lastStr = getStrAfterLastSep(path, "/")
        if (lastStr === "resume") {
          return "Resume"
        } else {
            return "Blog"
        }
      };
      $scope.state = getState($location.path())
      $scope.config = data
      return $scope.$on("$locationChangeSuccess", function(event, newLoc, oldLoc) {
        return $scope.state = getState($location.path())
      })
    })
  })
  
  // 获取各个标签
  app.controller('tagListCtrl', function($scope, $http, $routeParams) {
	  return $http.get("/HXBlog/blogListAction").success(function(data) {
		  var curTag = null
	      if (($routeParams.tag != null) && $routeParams.tag.length !== 0) {
	    	  curTag = $routeParams.tag
	        } else {
	        	curTag = "All"
	        }
		  
		  var tags = data.tagList
		  for(i=0; i<tags.length; i++) {
			  tags[i].href = tags[i].text + ".html"
			  if(tags[i].text == curTag) {
				  $scope.currentTag = tags[i]
			  }
		  }
		  
		  $scope.tagList = data.tagList
		  $scope.blogList = data.blogList
	  })
  });
  
  
//
//  app.controller('PostCtrl', function($scope, $http, $routeParams, indexService) {
//    return $http.get("post/" + $routeParams.postPath + ".md").success(function(data) {
//      $scope.postContent = data;
//      return indexService.async().then(function(data) {
//        var i, post, _i, _len, _results;
//        i = 0;
//        _results = [];
//        for (_i = 0, _len = data.length; _i < _len; _i++) {
//          post = data[_i];
//          if (post.path === $routeParams.postPath) {
//            $scope.prevPostPath = "";
//            $scope.nextPostPath = "";
//            if (data[i - 1] != null) {
//              $scope.prevPostPath = "#!/post/" + data[i - 1].path;
//            }
//            if (data[i + 1] != null) {
//              $scope.nextPostPath = "#!/post/" + data[i + 1].path;
//            }
//            break;
//          }
//          _results.push(i++);
//        }
//        return _results;
//      });
//    });
//  });
//
//  app.controller('ResumeCtrl', function($scope, $http) {
//    return $http.get("resume.json").success(function(data) {
//      return $scope.resume = data;
//    });
//  });

}).call(this);
