/*global module:false*/
module.exports = function(grunt) {

  // Project configuration.
  grunt.initConfig({
    meta: {
      version: '0.1.0',
      banner: '/*! PROJECT_NAME - v<%= meta.version %> - ' +
        '<%= grunt.template.today("yyyy-mm-dd") %>\n' +
        '* http://PROJECT_WEBSITE/\n' +
        '* Copyright (c) <%= grunt.template.today("yyyy") %> ' +
        'YOUR_NAME; Licensed MIT */'
    },

    lint: {
      files: ['grunt.js', 'ui/src/js/kif.js', 'test/**/*.js']
    },
    qunit: {
      files: ['test/**/*.html']
    },
    concat: {
      dist: {
        src: ['<banner:meta.banner>', '<file_strip_banner:src/FILE_NAME.js>'],
        dest: 'dist/FILE_NAME.js'
      }
    },
    min: {
      dist: {
        src: ['ui/src/js/kif.js'],
        dest: 'resources/public/js/kif.js'
      }
    },
    copy: {
      main: {
        files: [
            {src: ["ui/src/js/jquery*.js", "ui/src/js/json2.js", "ui/src/js/ZeroClipboard*.js"], dest: "resources/public/js/"},
            {src: ["ui/src/css/*.css"], dest: "resources/public/css/"},
            {src: ["ui/src/flash/*"], dest: "resources/public/flash/"},
            {src: ["ui/src/img/*"], dest: "resources/public/img/"},
            {src: ["ui/src/robots.txt"], dest: "resources/public/"}
        ]
      }
    },
    shell: {
      _options: {
        failOnError: true
      },
      make_js_resources: {
        command: 'mkdir -p resources/public/js'
      },
      make_css_resources: {
        command: 'mkdir -p resources/public/css'
      },
      make_flash_resources: {
        command: 'mkdir -p resources/public/flash'
      },
      make_img_resources: {
        command: 'mkdir -p resources/public/img'
      },
      lein_clean: {
        command: 'lein clean'
      },
      lein_deps: {
        command: 'lein deps'
      },
      lein_uberjar: {
        command: 'lein uberjar',
        stdout: true
      },
      lein_iplant_rpm: {
        command: 'lein iplant-rpm'
      },
      clean_resources: {
        command: 'rm -rf resources/'
      }
    },
    less: {
      build: {
        files: {
          "resources/public/css/kif.css" : "ui/src/css/kif.less"
        }
      }
    },
    watch: {
      files: ["ui/src/js/*.js", "ui/src/css/*.less", "ui/src/img/*", "ui/src/flash/*"],
      tasks: 'build-resources'
    },
    jshint: {
      options: {
        curly: true,
        eqeqeq: true,
        immed: true,
        latedef: true,
        newcap: true,
        noarg: true,
        sub: true,
        undef: true,
        boss: true,
        eqnull: true,
        browser: true
      },
      globals: {
        jQuery: true,
        $:true,
        Mustache: true,
        _: true,
        ZeroClipboard: true,
        zclip: true,
        alert: true
      }
    },
    uglify: {}
  });

  // Default task.
  grunt.registerTask('default', 'lint qunit concat min');
  grunt.loadNpmTasks('grunt-contrib-copy');
  grunt.loadNpmTasks('grunt-shell');
  grunt.loadNpmTasks('grunt-contrib-less');
  //grunt.loadNpmTasks('watch');

  grunt.registerTask('make-resources', 'shell:make_js_resources shell:make_css_resources shell:make_flash_resources shell:make_img_resources');
  grunt.registerTask('build-resources', 'lint make-resources less copy min');
  grunt.registerTask('build-resources-dev', 'lint make-resources less copy min');
  grunt.registerTask('build-clj', 'shell:lein_clean shell:lein_deps shell:lein_uberjar'); 
  grunt.registerTask('build-all', 'build-resources build-clj');
  grunt.registerTask('clean-all', 'shell:lein_clean shell:clean_resources');

};
