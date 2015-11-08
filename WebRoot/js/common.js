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
			 
			 // ��Ӵӷ�������ȡ��������
			var submitNoticePath = "#submitNotice"
			var commentPath = "dl.comment_topic"
			var replyDivPath = "div.replyDiv"
			var blogComments = data.comments
			console.log(blogComments)
			var floorLen = getLength(blogComments)
			if(floorLen > 0) {
				$("#haveNoComment").hide()
				for(i=0; i<floorLen; i++) {
					var curFloor = blogComments[i.toString() ]
					var commentLen = getLength(curFloor)
					appendNewFloorComment(commentPath, curFloor["0"])
					for(j=1; j<commentLen; j++) {
						appendNewReplyComment(commentPath, i+1, replyDivPath, curFloor[j.toString() ])
					}	
				}
			}
			
			// �ظ�, ����, �ٱ���ť��Ч��
			$(".comment_manage").find("a").mouseover(function() {
				$(this).css("color", "red")	
			})
			$(".comment_manage").find("a").mouseout(function() {
				$(this).css("color", "#3366B1")	
			})
			 
			 // ɾ�����Ӱ�ť
			$("#deleteAction").click(function() {
				var postUrl = "/HXBlog/action/blogDeleteAction"
				var blogObj = new Blog($scope.postId, null, null, null)
				$.ajax({
					url: postUrl, type : "post",
					data : blogObj.getObj(),
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
			
			 // ��ǰ�Ƿ���Ե�����Ȱ�ť
			 var couldClickGoodOrNotGood = true
			 
			// ����couldClickGoodOrNotGood
			function updateCouldClickGoodOrNotGood() {
				couldClickGoodOrNotGood = true
			}
			
			 // ���Ȱ�ť
			 $("dl[dataId='btnSense']").click( function() {
				 if(couldClickGoodOrNotGood) {
					 couldClickGoodOrNotGood = false
					 
					 var postUrl = "/HXBlog/action/blogSenseAction"
					 var sense = $(this).attr("data")
					 var other = $(this).siblings("dl[dataId='btnSense']")
					 if(isEmpty(sensed) ) {
						 sensed = sense;
						 var cur = parseInt($(this).find("dd").html() )
						  $(this).find("dd").html(cur + 1)
					 } else {
						 if(sense == sensed) {
							 sensed = EMPTY_STR
							 var cur = parseInt($(this).find("dd").html() )
							 $(this).find("dd").html(cur - 1)						 
						 } else {
							 sensed = sense
							 var cur = parseInt(other.find("dd").html() )
							 other.find("dd").html(cur - 1)									 
							 cur = parseInt($(this).find("dd").html() )
							 $(this).find("dd").html(cur + 1)
						 }  
					 }
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
					 console.log(couldClickGoodOrNotGood)
					 setTimeout(updateCouldClickGoodOrNotGood, 200)
//					 couldClickGoodOrNotGood = true
					 console.log("please do not click so quickly, robot ?")
				 }
			 
			})
			
			
			// ����
			var floorIdx = $(commentPath).length
//			var commentIdx = -1
			var to = EMPTY_STR
				
			// У��, �ύ����
				// ������˵�չ��, ��Ӧ���ǵ�ǰҳ��Ϊ׼, ˢ��һ�¼���̷������˵������Ļ���˳��
			$("#submitBtn").click(function() {
		    	$(submitNoticePath).html(EMPTY_STR)
		    	var userName = $("#userName").val().trim()
		    	var commentBody = $("#comment").val()
		    	var imageIdx = $("#imageIdx").find("option[selected='selected']").attr("id")
		    	if(validateTitle(userName, "userName", submitNoticePath)) {
		    		if(validateContent(commentBody, "comment", submitNoticePath)) {
						var comment = new Comment(data.blog.id, floorIdx, userName, to, $("#email").val().trim(), imageIdx, commentBody)
		    			var postUrl = "/HXBlog/action/blogCommentAction"
		    			console.log(comment.getObj() )
		    			
						$.ajax({
							url: postUrl, type : "post",
							data : comment.getObj(),
							success : function(data){
								$("#haveNoComment").hide()
								data = JSON.parse(data)
								comment = data.comment
								
								$("#submitCommentMsg").html(data.respMsg.msg)
								$("#submitCommentModal").modal()
								if(data.respMsg.isSuccess) {
									$("#comment").val(EMPTY_STR)
									if(! commentBody.startsWith("[re]")) {
										appendNewFloorComment(commentPath, comment)
									} else {
										appendNewReplyComment(commentPath, floorIdx, replyDivPath, comment)
									}
								}
								
								$(".commentReply").click(replayFunction)
								floorIdx = $(commentPath).length
					        }
						});   
		    		}
		    	}
			})
			
			// �ı���ͷ����¼�
			$("#imageIdx").change(function() {
				var curVal = $("#imageIdx").val()
				$("#imageIdx").find("option").attr("selected", false)
				$("#imageIdx").find("option[value='"+ curVal +"']").attr("selected", true)
			})
			
			$(".commentReply").click(replayFunction)
			// ���еĻظ���ť��click�¼�
			function replayFunction() {
				to = $(this).parent().parent().attr("userName")
				floorIdx = parseInt($(this).parent().parent().attr("floorIdx") )
//				commentIdx = $(this).parent().attr("commentIdx")
				$("#comment").val("[re]" + to + "[/re] : ")
			}
			 
		  })		  
	  }
  });
  
  // �������µ�controller
//  app.controller('publishCtrl', function($scope, $http) {
//  });  
  
  // ��ȡ������Ϣ��ctrl
  app.controller('resumeCtrl', function($scope, $http) {
    return $http.get("/HXBlog/action/blogResumeAction").success(function(data) {
      return $scope.resume = data;
    });
  });  
  
}).call(this);
