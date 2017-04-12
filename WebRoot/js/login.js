// µÇÂ¼Ö®Ç°, ¼ÓÃÜÃÜÂë
$("#submitBtn").click(function() {
	$("#pwd").val(hex_md5($("#pwd").val()) )
})