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
         var items = path.split("/");
        if (items.length > 1 && items[items.length - 1] === "resume") {
          return "Resume";
        } else {
            return "Blog";	
        }
      };
      $scope.state = getState($location.path());
      $scope.config = data;
      return $scope.$on("$locationChangeSuccess", function(event, newLoc, oldLoc) {
        return $scope.state = getState($location.path());
      });
    });
  });

//  app.controller('IndexListCtrl', function($scope, $routeParams, indexService) {
//    return indexService.async().then(function(data) {
//      var buildTagList, tag;
//      buildTagList = function(indexData) {
//        var all_tags, post, tag, tags, _i, _j, _len, _len2;
//        all_tags = [];
//        for (_i = 0, _len = indexData.length; _i < _len; _i++) {
//          post = indexData[_i];
//          all_tags = all_tags.concat(post.tags);
//        }
//        tags = {};
//        for (_j = 0, _len2 = all_tags.length; _j < _len2; _j++) {
//          tag = all_tags[_j];
//          if (tags[tag]) {
//            tags[tag]["count"] += 1;
//          } else {
//            tags[tag] = {
//              "text": tag,
//              "href": "#!/tag/" + tag,
//              "count": 1
//            };
//          }
//        }
//        tags["All"] = {
//          "text": "All",
//          "href": "#!/",
//          "count": indexData.length
//        };
//        return tags;
//      };
//      $scope.indexList = data;
//      indexService.indexData = data;
//      $scope.tagList = buildTagList(data);
//      if (($routeParams.tag != null) && $routeParams.tag.length !== 0) {
//        tag = $routeParams.tag;
//      } else {
//        tag = "All";
//      }
//      $scope.currentTag = $scope.tagList[tag];
//      if (tag === "All") {
//        return $scope.currentTag.filter = "";
//      } else {
//        return $scope.currentTag.filter = tag;
//      }
//    });
//  });
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
