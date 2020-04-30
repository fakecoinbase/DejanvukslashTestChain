const htmlWebpackPlugin = require('html-webpack-plugin');

module.exports = {
    devtool: 'cheap-module-source-map',
    module: {
        rules: [
            { test: /\.js$/, exclude: /node_modules/, loader: "babel-loader" },
            { test: /\.css$/, use: [
                'style-loader',
                'css-loader'
            ]},
            {
                test: /\.(jpg|png|gif)$/, loaders: ['file']
            }
        ]
    },
    devServer: {
        historyApiFallback: {
            index: '/',
        },
        hot: true
    },
    plugins: [
        new htmlWebpackPlugin({
            template: 'index.html'
        }),
        new webpack.HotModuleReplacementPlugin()
    ]
};