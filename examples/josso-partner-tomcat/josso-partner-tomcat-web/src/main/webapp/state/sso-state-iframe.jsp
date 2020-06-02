<!DOCTYPE html>
<html lang="en">
<body>
<script>

    setTimer();
    var timerID = null;

    function setTimer()
    {
        check_session();
        timerID = setInterval(check_session, 3*1000);
    }

    function check_session() {

        console.log("check_session");
        var oReq = new XMLHttpRequest();
        oReq.onreadystatechange = function () {

            if (this.readyState == XMLHttpRequest.DONE) {

                if (this.status == 200) {
                    let stat = this.responseText;
                    console.log("check_session:" + stat);
                    if (stat == 'changed') {
                        window.parent.location.reload();
                    }
                } else {
                    console.log(this.status);
                    console.log(oReq.responseText);
                    window.parent.location.reload();
                }
            }

        }

        oReq.open("GET", "<%=request.getContextPath()%>/state/sso-state.jsp");
        oReq.send();
    }

</script>
</body>
</html>
