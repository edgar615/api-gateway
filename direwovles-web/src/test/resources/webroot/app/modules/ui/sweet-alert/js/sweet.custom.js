/* Basic alerts */
document.querySelector('.basic-sweet-1').onclick = function(){
  swal("Here's a basic warning message!");
};

document.querySelector('.basic-sweet-2').onclick = function(){
  swal("Here's a basic success message!");
};

document.querySelector('.basic-sweet-3').onclick = function(){
  swal("Here's a basic info message!");
};

document.querySelector('.basic-sweet-4').onclick = function(){
  swal("Here's a basic danger message!");
};

/* Tagline alerts */
document.querySelector('.tagline-sweet-1').onclick = function(){
  swal({
    title: "An input!",
    text: 'Write something interesting:',
    type: 'input',
    showCancelButton: true,
    closeOnConfirm: false,
    animation: "slide-from-top"
  }, 
  function(inputValue){
    if (inputValue === false) return false;

    if (inputValue === "") {
      swal.showInputError("You need to write something!");
      return false;
    }
    
    swal("Nice!", 'You wrote: ' + inputValue, "success");
    
  });
};


document.querySelector('.tagline-sweet-2').onclick = function(){
  swal("Well done!", "You successfully read this important alert message.")
};

document.querySelector('.tagline-sweet-3').onclick = function(){
  swal("Heads up!", "This alert needs your attention, but it's not super important.")
};

document.querySelector('.tagline-sweet-4').onclick = function(){
  swal("Oh snap!", "Change a few things up and try submitting again.")
};

/* Alerts With Icons */
document.querySelector('.icon-sweet-1').onclick = function(){
  swal("Good job!", "You clicked the button!", "success");
};

document.querySelector('.icon-sweet-2').onclick = function(){
  swal("Heads up!", "This alert needs your attention, but it's not super important.", "info");
};

document.querySelector('.icon-sweet-3').onclick = function(){
  swal("Oh snap!", "Change a few things up and try submitting again.", "error");
};

document.querySelector('.icon-sweet-4').onclick = function(){
  swal("Warning!", "Best check yo self, you're not looking too good.", "warning");
};

/* Icon Alerts With confirm Button */
document.querySelector('.confirm-sweet-1').onclick = function(){
  swal({
    title: "Are you sure?",
    text: "You will not be able to recover this imaginary file!",
    type: "error",
    showCancelButton: true,
    confirmButtonClass: 'btn-danger',
    confirmButtonText: 'Yes, delete it!',
    closeOnConfirm: false,
          //closeOnCancel: false
      },
      function(){
        swal("Deleted!", "Your imaginary file has been deleted!", "success");
      });
};

document.querySelector('.confirm-sweet-2').onclick = function(){
  swal({
    title: "You are Awesome",
    text: "Thankyou for viewing our template",
    type: "success",
    showCancelButton: true,
    confirmButtonClass: 'btn-success',
    confirmButtonText: 'Welcome!',
    closeOnConfirm: false,
          //closeOnCancel: false
      },
      function(){
        swal("Thanks!", "We are glad you clicked welcome!", "success");
      });
};

document.querySelector('.confirm-sweet-3').onclick = function(){
  swal({
    title: "Cool Tip!",
    text: "You have to check every feature with patience",
    type: "info",
    showCancelButton: true,
    confirmButtonClass: 'btn-info',
    confirmButtonText: 'Ok! got it',
    closeOnConfirm: false,
          //closeOnCancel: false
      },
      function(){
        swal("Good!", "Thanks for clicking!", "success");
      });
};

document.querySelector('.confirm-sweet-4').onclick = function(){
  swal({
    title: "Oops!",
    text: "I'am afraid you are missing something",
    type: "warning",
    showCancelButton: true,
    confirmButtonClass: 'btn-warning',
    confirmButtonText: 'I will check it',
    closeOnConfirm: false,
          //closeOnCancel: false
      },
      function(){
        swal("Great!", "Thanks for checking it!", "success");
      });
};

/* Advanced Alerts */

document.querySelector('.advanced-sweet-1').onclick = function(){
  swal({
    title: "Are you sure?",
    text: "You will not be able to recover this imaginary file!",
    type: "warning",
    showCancelButton: true,
    confirmButtonColor: '#6fd64b',
    confirmButtonText: 'Yes, delete it!',
    cancelButtonText: "No, cancel plx!",
    closeOnConfirm: false,
    closeOnCancel: false
  },
  function(isConfirm){
    if (isConfirm){
      swal("Deleted!", "Your imaginary file has been deleted!", "success");
    } else {
      swal("Cancelled", "Your imaginary file is safe :)", "error");
    }
  });
};
document.querySelector('.advanced-sweet-2').onclick = function(){
  swal({
    title: "Are you sure?",
    text: "You will not be able to recover this imaginary file!",
    type: "warning",
    showCancelButton: true,
    confirmButtonColor: '#6cadd1',
    confirmButtonText: 'Yes, delete it!',
    cancelButtonText: "No, cancel plx!",
    closeOnConfirm: false,
    closeOnCancel: false
  },
  function(isConfirm){
    if (isConfirm){
      swal("Deleted!", "Your imaginary file has been deleted!", "success");
    } else {
      swal("Cancelled", "Your imaginary file is safe :)", "error");
    }
  });
};
document.querySelector('.advanced-sweet-3').onclick = function(){
  swal({
    title: "Are you sure?",
    text: "You will not be able to recover this imaginary file!",
    type: "warning",
    showCancelButton: true,
    confirmButtonColor: '#fb5d5d',
    confirmButtonText: 'Yes, delete it!',
    cancelButtonText: "No, cancel plx!",
    closeOnConfirm: false,
    closeOnCancel: false
  },
  function(isConfirm){
    if (isConfirm){
      swal("Deleted!", "Your imaginary file has been deleted!", "success");
    } else {
      swal("Cancelled", "Your imaginary file is safe :)", "error");
    }
  });
};
document.querySelector('.advanced-sweet-4').onclick = function(){
  swal({
    title: "Are you sure?",
    text: "You will not be able to recover this imaginary file!",
    type: "warning",
    showCancelButton: true,
    confirmButtonColor: '#eac841',
    confirmButtonText: 'Yes, delete it!',
    cancelButtonText: "No, cancel plx!",
    closeOnConfirm: false,
    closeOnCancel: false
  },
  function(isConfirm){
    if (isConfirm){
      swal("Deleted!", "Your imaginary file has been deleted!", "success");
    } else {
      swal("Cancelled", "Your imaginary file is safe :)", "error");
    }
  });
};
document.querySelector('.custom-sweet-1').onclick = function(){
  swal({
    title: "Sweet!",
    text: "Here's a custom image.",
    imageUrl: 'assets/images/avatar/ten.png'
  });
};
document.querySelector('.custom-sweet-2').onclick = function(){
  swal({
    title: "Sweet!",
    text: "Here's a custom image.",
    imageUrl: 'assets/images/avatar/three.png'
  });
};
document.querySelector('.custom-sweet-3').onclick = function(){
  swal({
    title: "Sweet!",
    text: "Here's a custom image.",
    imageUrl: 'assets/images/avatar/one.png'
  });
};
document.querySelector('.custom-sweet-4').onclick = function(){
  swal({
    title: "Sweet!",
    text: "Here's a custom image.",
    imageUrl: 'assets/images/avatar/eight.png'
  });
};