(function() {
  var app = angular.module('blog', ['ngSanitize'])
  
  // 获取各个标签
  app.controller('tagListCtrl', function($scope, $http, $location) {
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
  
  
  
}).call(this);